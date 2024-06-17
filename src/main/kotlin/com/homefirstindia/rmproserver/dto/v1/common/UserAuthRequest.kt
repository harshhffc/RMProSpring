package com.homefirstindia.rmproserver.dto.v1.common

import com.homefirstindia.rmproserver.utils.*
import javax.servlet.http.HttpServletRequest

class UserAuthRequest() {

    var userId = -1
    var email = NA
    var password = NA
    var sessionPasscode = NA
    var sourcePasscode = NA
    var crownPasscode = NA
    var ipAddress = NA
    var requestUri = NA

    fun isMandatoryInformationPresent(): LocalResponse {

        val localResponse = LocalResponse()
            .setAction(Actions.FIX_RETRY.value)
            .setError(Errors.INVALID_DATA.value)

        return when {
            !email.isValidEmail() -> localResponse.apply { message = "Invalid email." }
            else -> {
                localResponse.apply {
                    isSuccess = true
                    message = AUTHORIZED
                    action = NA
                    error = NA
                }
            }
        }

    }

    constructor(request: HttpServletRequest) : this() {

        request.run {

            getHeader(USER_SOURCE_ID)?.let {
                if (it.isNotNullOrNA())
                    userId = it.toInt()
            }

            ipAddress = remoteAddr
            requestUri = requestURI

            getHeader(SESSION_PASSCODE)?.let {
                sessionPasscode = it
            }

            getHeader(SOURCE_PASSCODE)?.let {
                sourcePasscode = it
            }

            getHeader(CROWN_PASSCODE)?.let {
                crownPasscode = it
            }

        }

    }

}