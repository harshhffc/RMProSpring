package com.homefirstindia.rmproserver.dto.v1.externalPartner

import com.homefirstindia.rmproserver.utils.*
import javax.servlet.http.HttpServletRequest


class EPAuthRequest() {

    var authorization = NA
    var clientId = NA
    var clientSecret = NA
    var orgId = NA
    var sessionPasscode = NA
    var ipAddress = NA
    var requestUri = NA

    constructor(request: HttpServletRequest): this() {

        request.run {

            authorization = getHeader(AUTHORIZATION)
            orgId = getHeader(ORG_ID)
            ipAddress = getIPAddress(this)
            requestUri = requestURI

            getHeader(SESSION_PASSCODE)?.let {
                sessionPasscode = it
            }

            getClientCreds(authorization).let {
                clientId = it.clientId
                clientSecret = it.clientSecret
            }

        }

    }

    fun isRequestValid(): Boolean {
        return authorization.isNotNullOrNA() && orgId.isNotNullOrNA()
    }

}

class BasicAuthCreds(
        var clientId: String = NA,
        var clientSecret: String = NA
)






