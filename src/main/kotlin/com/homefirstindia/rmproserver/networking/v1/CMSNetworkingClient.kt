package com.homefirstindia.rmproserver.networking.v1

import com.homefirstindia.rmproserver.manager.v1.EnPartnerName
import com.homefirstindia.rmproserver.model.v1.Creds
import com.homefirstindia.rmproserver.repository.v1.CredsRepository
import com.homefirstindia.rmproserver.security.AppProperty
import com.homefirstindia.rmproserver.utils.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CMSNetworkingClient  (
    @Autowired val appProperty: AppProperty,
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired val credsRepo: CredsRepository,
    @Autowired var commonNetworkingClient: CommonNetworkingClient
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    private var _creds: Creds? = null

    companion object {
        private var SESSION_PASSCODE: String? = null
    }

    private var retryCount = 0

    enum class Endpoints(val value: String) {
        AUTHENTICATE("/v1/ep/authenticate"),
        ADD_REMARK("/v1/ep/addRemark"),
        GET_REMARK("/v1/ep/getRemarks")
    }

    private fun newApiRequest(): CommonNetworkingClient.NewRequest {
        return commonNetworkingClient
            .NewRequest()
            .addHeader(
                "Authorization",
                "Basic " + cryptoUtils.encodeBase64("${cmsCreds().username}:${cmsCreds().password}")
            )
            .addHeader("orgId", cmsCreds().memberPasscode!!)
            .addHeader("sessionPasscode", SESSION_PASSCODE ?: NA)
    }

    @Throws(Exception::class)
    fun get(url: String): LocalHTTPResponse {

        authenticateCMSClient()

        val fullUrl = "${cmsCreds().apiUrl}${url}"

        val localHTTPResponse = newApiRequest()
            .getCall(fullUrl)
            .send()

        if (localHTTPResponse.statusCode == 401) {

            if (retryCount < 3) {

                retryCount++
                reAuthenticate()
                return get(url)

            } else retryCount = 0
        }

        return localHTTPResponse

    }


    @Throws(Exception::class)
    fun post(url: String, requestJson: JSONObject): LocalHTTPResponse {

        authenticateCMSClient()

        val fullUrl = "${cmsCreds().apiUrl}${url}"

        val localHTTPResponse = newApiRequest()
            .postCall(fullUrl, requestJson)
            .send()

        if (localHTTPResponse.statusCode == 401) {

            if (retryCount < 3) {

                retryCount++
                reAuthenticate()
                return post(url, requestJson)

            } else retryCount = 0
        }

        return localHTTPResponse

    }



    @Throws(Exception::class)
    private fun authenticateCMSClient() {

        try {

            cmsCreds()

            if (isNotNullOrNA(SESSION_PASSCODE)) return

            val fullUrl = "${cmsCreds().apiUrl}${Endpoints.AUTHENTICATE.value}"

            val cmsResponse = newApiRequest().getCall(fullUrl).send()

            log("CMS response code: ${cmsResponse.statusCode} body: ${cmsResponse.stringEntity}")

            val lsJsonResponse = JSONObject(cmsResponse.stringEntity)

            when (cmsResponse.statusCode) {
                200 -> {
                    log("CMS Client authorized successfully.")
                    SESSION_PASSCODE = lsJsonResponse.getString("sessionPasscode")
                }
                401 -> {
                    log("Unauthorized access while authenticateCMSClient.")
                    throw Exception("Unauthorized access while authenticateCMSClient.")
                }
                else -> {
                    val errorMessage = lsJsonResponse.optString(MESSAGE, "Error while authenticateCMSClient.")
                    log("Error while authenticateCMSClient: $errorMessage")
                    throw Exception(errorMessage)
                }
            }
        } catch (e: Exception) {
            log("Error while authenticateCMSClient: " + e.message)
            e.printStackTrace()
            throw e
        }
    }

    @Throws(Exception::class)
    private fun cmsCreds(): Creds {
        if (null == _creds || null == SESSION_PASSCODE) {

            _creds = credsRepo.findByPartnerNameAndCredType(
                EnPartnerName.CMS.value,
                if (appProperty.isProduction()) CredType.PRODUCTION.value else CredType.UAT.value
            )
            if (null == _creds) {
                log("cmsCreds - failed to get CMS Creds from DB.")
                throw Exception("failed to get CMS Creds from DB.")
            }
        }
        return _creds!!
    }

    @Throws(Exception::class)
    private fun reAuthenticate() {
        SESSION_PASSCODE = null
        authenticateCMSClient()
    }

}
