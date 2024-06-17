package com.homefirstindia.rmproserver.networking.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.rmproserver.manager.v1.CredsManager
import com.homefirstindia.rmproserver.manager.v1.EnCredType
import com.homefirstindia.rmproserver.manager.v1.EnPartnerName
import com.homefirstindia.rmproserver.model.v1.Creds
import com.homefirstindia.rmproserver.utils.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import okhttp3.RequestBody
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class HFONetworkClient(
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired val credentialManager: CredsManager,
    @Autowired val objectMapper: ObjectMapper
) {

    private fun log(value: String) = LoggerUtils.log("HFONetworkClient.$value")

    companion object {
        private var SESSION_PASSCODE: String? = null
        private const val TIMEOUT = 60
        private var _hfoCred: Creds? = null
    }

    private var retryCount = 0

    private var client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS).build()

    enum class Endpoints(val value: String) {
        AUTHENTICATE_CLIENT("/ep/v1/authenticate"),
        LOCATION_DISTANCE_MATRIX("/ep/v1/Location.distanceMatrix"),
        LOCATION_DIRECTIONS("/ep/v1/Location.directions"),
        CALL_LOG_ADVANCED_FILTER("/com/v1/CallLogs.advanceFilter"),
        CALL_LOG_DETAIL("/com/v1/CallLog.detail"),
        CALL_LOG_REMARK("/com/v1/CallLog.addRemark");
    }

    @Throws(Exception::class)
    private fun hfoCred(): Creds {
        if (null == _hfoCred) {

            _hfoCred = credentialManager.fetchCredentials(
                EnPartnerName.HFO_SPRING,
                when {
                    cryptoUtils.appProperty.isProduction() -> EnCredType.PRODUCTION
                    else -> EnCredType.UAT
                }
            )
            if (null == _hfoCred) {
                log("hfoCred - failed to get HomefirstOne Creds from DB.")
                throw Exception("failed to get HomefirstOne Creds from DB.")
            }
        }
        return _hfoCred!!
    }

    private fun newHttpBuilder(): Builder {

        val hfoCredential = hfoCred()

        return Builder()
            .addHeader(
                "Authorization",
                "Basic " + cryptoUtils.encodeBase64("${hfoCredential.username}:${hfoCredential.password}")
            )
            .addHeader("orgId", hfoCredential.memberPasscode!!)
            .addHeader("sessionPasscode", SESSION_PASSCODE ?: NA)
    }

    @Throws(Exception::class)
    fun get(endPoint: Endpoints, params: String? = null): LocalResponse {

        authenticateHFOClient()

        var fullUrl = "${hfoCred().apiUrl}${endPoint.value}"

        if (params.isNotNullOrNA()) {
            fullUrl += params
        }

        val request = newHttpBuilder()
            .url(fullUrl)
            .method("GET", null)
            .build()

        val response = client.newCall(request).execute()
        val responseString = response.body!!.string()
        val responseCode = response.code
        response.body?.close()
        response.close()

        log("HFO GET response code: $responseCode body: $responseString")

        val localHTTPResponse = LocalResponse()
        localHTTPResponse.response = responseString

        if (responseCode == 200) {
            retryCount = 0
            localHTTPResponse.isSuccess = true
        } else if (responseCode == 401) {
            log("Unauthorized access while GET.")
            if (retryCount < 3) {
                retryCount++
                reAuthenticateClient()
                return get(endPoint, params)
            } else {
                retryCount = 0
                localHTTPResponse.isSuccess = false
                localHTTPResponse.message = "Unauthorized access."
            }
        } else {
            retryCount = 0
            localHTTPResponse.isSuccess = false
            localHTTPResponse.getErrorMessage()
        }
        return localHTTPResponse
    }

    @Throws(Exception::class)
    fun post(endPoint: Endpoints, requestJson: JSONObject): LocalResponse {

        authenticateHFOClient()

        val fullUrl = "${hfoCred().apiUrl}${endPoint.value}"

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, requestJson.toString())
        val request = newHttpBuilder()
            .url(fullUrl)
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseString = response.body!!.string()
        val responseCode = response.code
        response.body!!.close()
        response.close()

        log("HFO POST response code: $responseCode body: $responseString")

        val localResponse = LocalResponse()
        localResponse.response = responseString

        when (responseCode) {
            200 -> {
                retryCount = 0
                localResponse.isSuccess = true
            }

            401 -> {
                log("Unauthorized access while POST. Retry Count: $retryCount")
                if (retryCount < 3) {
                    retryCount++
                    reAuthenticateClient()
                    return post(endPoint, requestJson)
                } else {
                    retryCount = 0
                    localResponse.isSuccess = false
                    localResponse.message = "Unauthorized access."
                }
            }

            else -> {
                retryCount = 0
                localResponse.isSuccess = false
                localResponse.getErrorMessage()
            }
        }

        return localResponse
    }

    @Throws(Exception::class)
    fun post(url: String, requestJson: JSONObject): LocalResponse {

        authenticateHFOClient()

        val fullUrl = "${hfoCred().apiUrl}${url}"

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, requestJson.toString())
        val request = newHttpBuilder()
            .url(fullUrl)
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseString = response.body!!.string()
        val responseCode = response.code
        response.body!!.close()
        response.close()

        log("HFO POST response code: $responseCode body: $responseString")

        val localResponse = LocalResponse()
        localResponse.response = responseString

        when (responseCode) {
            200 -> {
                retryCount = 0
                localResponse.isSuccess = true
            }

            401 -> {
                log("Unauthorized access while POST. Retry Count: $retryCount")
                if (retryCount < 3) {
                    retryCount++
                    reAuthenticateClient()
                    return post(url, requestJson)
                } else {
                    retryCount = 0
                    localResponse.isSuccess = false
                    localResponse.message = "Unauthorized access."
                }
            }

            else -> {
                retryCount = 0
                localResponse.isSuccess = false
                localResponse.getErrorMessage()
            }
        }

        return localResponse
    }

    @Throws(Exception::class)
    private fun reAuthenticateClient() {
        SESSION_PASSCODE = null
        authenticateHFOClient()
    }

    @Throws(Exception::class)
    private fun authenticateHFOClient() {

        try {

            if (isNotNullOrNA(SESSION_PASSCODE)) return

            val fullUrl = "${hfoCred().apiUrl}${Endpoints.AUTHENTICATE_CLIENT.value}"

            val client = OkHttpClient().newBuilder().build()
            val request = newHttpBuilder()
                .url(fullUrl)
                .method("GET", null)
                .build()

            val lsResponse = client.newCall(request).execute()
            val responseEntity = lsResponse.body!!.string()

            log("authenticateHFOClient - Response body: $responseEntity")

            val lsJsonResponse = JSONObject(responseEntity)
            val lsResponseCode = lsResponse.code
            lsResponse.body!!.close()
            lsResponse.close()

            log("HFO response code: $lsResponseCode body: $lsJsonResponse")

            when (lsResponseCode) {
                200 -> {
                    log("HFO Client authorized successfully.")
                    SESSION_PASSCODE = lsJsonResponse.getString("sessionPasscode")
                }

                401 -> {
                    log("Unauthorized access while authenticateCPClient.")
                    throw Exception("Unauthorized access while authenticateCPClient.")
                }

                else -> {
                    val errorMessage = lsJsonResponse.optString(MESSAGE, "Error while authenticateCPClient.")
                    log("Error while authenticateCPClient: $errorMessage")
                    throw Exception(errorMessage)
                }
            }
        } catch (e: Exception) {
            log("Error while authenticateHFOClient: " + e.message)
            e.printStackTrace()
            throw e
        }
    }

}