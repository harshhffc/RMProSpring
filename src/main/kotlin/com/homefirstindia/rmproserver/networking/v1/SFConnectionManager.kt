package com.homefirstindia.rmproserver.networking.v1

import com.homefirstindia.rmproserver.manager.v1.CredsManager
import com.homefirstindia.rmproserver.manager.v1.EnCredType
import com.homefirstindia.rmproserver.manager.v1.EnPartnerName
import com.homefirstindia.rmproserver.model.v1.Creds
import com.homefirstindia.rmproserver.utils.*
import org.apache.http.Header
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.*
import java.net.URLEncoder

const val SO_OBJECT = "sobjects"

enum class EnSFObjects(val value: String) {
    CONTACT("/$SO_OBJECT/Contact/"),
    PROPERTY_INSIGHT("/sobjects/Property_Insight__c/");
}

@Component
class SFConnection(
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired val credentialManager: CredsManager
) {

    companion object {

        private const val GRANT_SERVICE = "/services/oauth2/token?grant_type=password"
        private const val REST_ENDPOINT = "/services/data"
        private const val APEX_REST_ENDPOINT = "/services/apexrest"
        private const val API_VERSION = "/v50.0"

        private var _sfCredentials: Creds? = null

    }

    var baseUri: String? = null
    private var instanceUri: String? = null
    var apexBaseUri: String? = null
    private var oauthHeader: Header? = null
    private val prettyPrintHeader: Header = BasicHeader("X-PrettyPrint", "1")
    private var retryCount = 0

    private fun log(value: String) {
        LoggerUtils.log("SFConnection.$value")
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getModifiedQuery(query: String): String? {
        return URLEncoder.encode(query, "UTF-8")
    }

    private fun getBody(inputStream: InputStream): String? {
        var result: String? = ""
        try {
            val `in` = BufferedReader(InputStreamReader(inputStream))
            var inputLine: String?
            while (`in`.readLine().also { inputLine = it } != null) {
                result += inputLine
                result += "\n"
            }
            `in`.close()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        return result
    }

    @Throws(Exception::class)
    private fun checkAndAuthenticate() {
        if (null == baseUri || null == oauthHeader) authenticate()
    }

    private fun sfCredentials(): Creds? {
        return _sfCredentials ?: credentialManager.fetchCredentials(
            EnPartnerName.SALESFORCE,
            if (cryptoUtils.appProperty.isSalesforceLive) EnCredType.PRODUCTION else EnCredType.PRE_PROD
        )?.also {
            _sfCredentials = it
        }
    }

    @Throws(Exception::class)
    private fun authenticate() {

        val sfCred = sfCredentials() ?: throw Exception("Failed to get Salesforce credentials")

        val httpclient: HttpClient = HttpClientBuilder.create().build()

        val loginURL = "${sfCred.apiUrl}$GRANT_SERVICE" +
                "&client_id=${decryptAnyKey(sfCred.memberId!!)}" +
                "&client_secret=${decryptAnyKey(sfCred.memberPasscode!!)}" +
                "&username=${decryptAnyKey(sfCred.username!!)}" +
                "&password=${decryptAnyKey(sfCred.password!!)}"

//        log("authenticate - loginURL : $loginURL")

        val httpPost = HttpPost(loginURL)
        val response: HttpResponse

        try {
            response = httpclient.execute(httpPost)
        } catch (cpException: ClientProtocolException) {
            log("authenticate - Error ClientProtocolException - ${cpException.message}")
            cpException.printStackTrace()
            throw cpException
        } catch (ioException: IOException) {
            log("authenticate - Error IOException - ${ioException.message}")
            ioException.printStackTrace()
            throw ioException
        }

        val statusCode: Int = response.statusLine.statusCode
        if (statusCode != HttpStatus.SC_OK) {
            log("authenticate - Error authenticating to Force.com: $statusCode | error: ${response.statusLine}")
            log("authenticate - Error ------- : ${EntityUtils.toString(response.entity)}")
            throw Exception("authenticate - Error ------- : ${EntityUtils.toString(response.entity)}")
        }

        val getResult: String?
        try {
            getResult = EntityUtils.toString(response.entity)
        } catch (ioException: IOException) {
            log("authenticate - Error while get entity from the response: ${ioException.message}")
            ioException.printStackTrace()
            throw ioException
        }
        val jsonObject: JSONObject?
        val loginAccessToken: String?
        val loginInstanceUrl: String?

        try {

            jsonObject = JSONTokener(getResult).nextValue() as JSONObject

            log("authenticate -  received data: $jsonObject")

            loginAccessToken = jsonObject.getString("access_token")
            loginInstanceUrl = jsonObject.getString("instance_url")

        } catch (jsonException: JSONException) {
            jsonException.printStackTrace()
            log("authenticate - Error while getting json from response: ${jsonException.message}")
            throw jsonException
        }

        instanceUri = loginInstanceUrl
        baseUri = loginInstanceUrl + REST_ENDPOINT + API_VERSION
        apexBaseUri = loginInstanceUrl + APEX_REST_ENDPOINT
        oauthHeader = BasicHeader("Authorization", "OAuth $loginAccessToken")

        println(response.statusLine)
        println("Successful login")
        println("  instance URL: $loginInstanceUrl")
        println("  access token/session ID: $loginAccessToken")
        println("baseUri: $baseUri")

        httpPost.releaseConnection()

    }

    @Throws(Exception::class)
    fun get(query: String): JSONObject? {

        return try {

            checkAndAuthenticate()

            log("get - Salesforce query: $query")
            // Set up the HTTP objects needed to make the request.
            val httpClient: HttpClient = HttpClientBuilder.create().build()
            val uri = baseUri + "/query?q=" + getModifiedQuery(query)
            println("Query URL: $uri")
            val httpGet = HttpGet(uri)
            // System.out.println("oauthHeader2: " + oauthHeader);
            httpGet.addHeader(oauthHeader)
            httpGet.addHeader(prettyPrintHeader)
            // Make the request.
            val response = httpClient.execute(httpGet)
            // Process the result
            val statusCode = response.statusLine.statusCode
            if (statusCode == 200) {
                retryCount = 0
                val responseString = EntityUtils.toString(response.entity)
                try {
                    val json = JSONObject(responseString)
                    println("JSON result of Query:\n$json")
                    json
                } catch (je: JSONException) {
                    je.printStackTrace()
                    null
                }
            } else if (statusCode == 401 && retryCount < 3) {
                println("Query was unsuccessful. Access token was expired: $statusCode")
                baseUri = null
                oauthHeader = null
                retryCount++
                authenticate()
                get(query)
            } else {
                retryCount = 0
                println("Query was unsuccessful. Status code returned is $statusCode")
                println("An error has occured. Http status: " + response.statusLine.statusCode)
                println(getBody(response.entity.content))
                // System.exit(-1);
                null
            }
        } catch (e: java.lang.Exception) {
            log("get - error while getting data from salesforce: $e")
            e.printStackTrace()
            throw e
        }
    }

    fun apexGet(uri: String): JSONObject? {

        return try {

            checkAndAuthenticate()


            val endPoint = apexBaseUri.plus(uri)
            log("apexGet - query url : $endPoint")

            val httpClient: HttpClient = HttpClientBuilder.create().build()
            val httpGet = HttpGet(endPoint)
            httpGet.addHeader(oauthHeader)
            httpGet.addHeader(prettyPrintHeader)
            val response = httpClient.execute(httpGet)
            val statusCode = response.statusLine.statusCode
            if (statusCode == 200) {
                retryCount = 0
                val responseString = EntityUtils.toString(response.entity)
                println("apexGet  response: $responseString")
                try {
                    val json = JSONObject(responseString)
                    println("JSON result of Query:\n$json")
                    json
                } catch (je: JSONException) {
                    je.printStackTrace()
                    null
                }
            } else if (statusCode == 401 && retryCount < 3) {
                println("Query was unsuccessful. Access token was expired: $statusCode")
                baseUri = null
                oauthHeader = null
                retryCount++
                authenticate()
                apexGet(uri)
            } else {
                retryCount = 0
                println("Query was unsuccessful. Status code returned is $statusCode")
                println("An error has occurred. Http status: " + response.statusLine.statusCode)
                println(getBody(response.entity.content))
                // System.exit(-1);
                null
            }
        } catch (e: Exception) {
            log("apexGet - Error : " + e.message)
            e.printStackTrace()
            null
        }
    }

    @Throws(java.lang.Exception::class)
    fun post(requestObject: JSONObject, sfObject: EnSFObjects): LocalHTTPResponse {

        checkAndAuthenticate()

        val lhResponse = LocalHTTPResponse()
        val uri = baseUri + sfObject.value
        val httpClient: HttpClient = HttpClientBuilder.create().build()
        val httpPost = HttpPost(uri)
        httpPost.addHeader(oauthHeader)
        httpPost.addHeader(prettyPrintHeader)
        val body = StringEntity(requestObject.toString(1))
        body.setContentType(CONTENT_TYPE_APPLICATION_JSON)
        httpPost.entity = body
        val response = httpClient.execute(httpPost)
        val statusCode = response.statusLine.statusCode
        if (statusCode == 201) {
            retryCount = 0
            val responseString = EntityUtils.toString(response.entity)
            println("POST - Response: $responseString")
            lhResponse.isSuccess = true
            lhResponse.statusCode = statusCode
            lhResponse.stringEntity = responseString
        } else if (statusCode == 401 && retryCount < 3) {
            println("POST - Call was unsuccessful. Access token was expired: $statusCode")
            baseUri = null
            oauthHeader = null
            retryCount++
            authenticate()
            return post(requestObject, sfObject)
        } else {
            retryCount = 0
            log("POST - Call unsuccessful. Status code returned is $statusCode | error: ${response.statusLine}")
            val respArray = JSONArray(getBody(response.entity.content))
            val respObj = respArray.get(0) as JSONObject
            val errorCode = respObj.optString("errorCode", NA)
            log("POST - Error : $errorCode | Response: $respObj")
            lhResponse.isSuccess = false
            lhResponse.statusCode = statusCode
            lhResponse.message = errorCode
            lhResponse.errorMessage = respObj.toString()

        }
        return lhResponse
    }

    @Throws(java.lang.Exception::class)
    fun patch(requestObject: JSONObject, endpoint: String?): LocalHTTPResponse? {

        checkAndAuthenticate()

        val lhResponse = LocalHTTPResponse()
        val httpClient: HttpClient = HttpClientBuilder.create().build()
        val uri = baseUri + endpoint

        val httpPatch = HttpPatch(uri)
        httpPatch.addHeader(oauthHeader)
        httpPatch.addHeader(prettyPrintHeader)
        val body = StringEntity(requestObject.toString(1))
        body.setContentType(CONTENT_TYPE_APPLICATION_JSON)
        httpPatch.entity = body
        val response = httpClient.execute(httpPatch)
        val statusCode = response.statusLine.statusCode
        if (statusCode == 204) {
            retryCount = 0
            lhResponse.isSuccess = true
            lhResponse.statusCode = statusCode
            lhResponse.stringEntity = NA
        } else if (statusCode == 401 && retryCount < 3) {
            println("PATCH Call was unsuccessful. Access token was expired: $statusCode")
            baseUri = null
            oauthHeader = null
            retryCount++
            authenticate()
            return patch(requestObject, uri)
        } else {
            retryCount = 0
            log("PATCH - Call unsuccessful. Status code returned is  $statusCode  | error: ${response.statusLine}")
            val respArray = JSONArray(getBody(response.entity.content))
            val respObj = respArray.get(0) as JSONObject
            val errorCode = respObj.optString("errorCode", NA)
            log("PATCH - Call Error : $errorCode | Response: $respObj")
            lhResponse.isSuccess = false
            lhResponse.statusCode = statusCode
            lhResponse.message = errorCode
        }
        return lhResponse
    }

    fun getNextRecords(nextRecordsUrl: String): JSONObject? {
        try {

            val httpClient: HttpClient = HttpClientBuilder.create().build()

            val uri = instanceUri + nextRecordsUrl
            val httpGet = HttpGet(uri)
            httpGet.addHeader(oauthHeader)
            httpGet.addHeader(prettyPrintHeader)
            val response = httpClient.execute(httpGet)
            val statusCode = response.statusLine.statusCode
            if (statusCode == 200) {
                retryCount = 0
                val responseString = EntityUtils.toString(response.entity)
                return try {
                    val json = JSONObject(responseString)
                    println("JSON result of More Query:\n$json")
                    json
                } catch (je: JSONException) {
                    je.printStackTrace()
                    null
                }
            } else if (statusCode == 401 && retryCount < 3) {
                println("More Query was unsuccessful. Access token was expired: $statusCode")
                baseUri = null
                oauthHeader = null
                retryCount++
                authenticate()
                return getNextRecords(nextRecordsUrl)
            } else {
                retryCount = 0
                println("More Query was unsuccessful. Status code returned is $statusCode")
                println("An error has occurred. Http status: " + response.statusLine.statusCode)
                println(getBody(response.entity.content))
                // System.exit(-1);
                return null
            }
        } catch (e: java.lang.Exception) {
            log("getNextRecords - Error while getting More data from salesforce: $e")
            e.printStackTrace()
            return null
        }
    }

    fun getAttachmentData(documentId: String): String? {

        var documentData: String? = null

        try {

            val httpClient: HttpClient = HttpClientBuilder.create().build()
            val uri = "$baseUri/sobjects/Attachment/$documentId/Body"
            println("Query URL: $uri")
            val httpGet = HttpGet(uri)
            println("oauthHeader2: $oauthHeader")
            httpGet.addHeader(oauthHeader)
            httpGet.addHeader(prettyPrintHeader)
            val response = httpClient.execute(httpGet)
            val statusCode = response.statusLine.statusCode
            if (statusCode == 200) {
                retryCount = 0
                try {

//                    val encoded: ByteArray = Base64.getEncoder().encode(IOUtils.toByteArray(response.entity.content))
//                    documentData = String(encoded)

                    documentData = cryptoUtils.encodeBase64(response.entity.content)

                } catch (je: JSONException) {
                    je.printStackTrace()
                    return null
                }
            } else if (statusCode == 401 && retryCount < 3) {
                println("Query was unsuccessful. Access token was expired: $statusCode")
                baseUri = null
                oauthHeader = null
                retryCount++
                authenticate()
                return getAttachmentData(documentId)
            } else {
                retryCount = 0
                println("Query was unsuccessful. Status code returned is $statusCode")
                println("An error has occurred. Http status: " + response.statusLine.statusCode)
                println(getBody(response.entity.content))
                // System.exit(-1);
                return null
            }
        } catch (e: java.lang.Exception) {
            log("getAttachmentData - Failed to get attachment for for id: $documentId")
            e.printStackTrace()
        }
        return documentData
    }

}