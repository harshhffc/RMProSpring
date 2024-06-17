package com.homefirstindia.rmproserver.controller.v1

import com.homefirstindia.rmproserver.dto.v1.externalPartner.EPAuthRequest
import com.homefirstindia.rmproserver.services.v1.ExternalPartnerService
import com.homefirstindia.rmproserver.utils.ID
import com.homefirstindia.rmproserver.utils.LoggerUtils
import com.homefirstindia.rmproserver.utils.OneResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/ep/v1")
class ExternalPartnerController(
    @Autowired val oneResponse: OneResponse,
    @Autowired val externalPartnerService: ExternalPartnerService
) {

    private fun logMethod(value: String) = LoggerUtils.logMethodCall("/ep/v1/$value")

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    @GetMapping(
            "/authenticate",
            produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epAuthenticate(
            request: HttpServletRequest
    ) : ResponseEntity<String>?  {

        logMethod("authenticate")

        return try {
            externalPartnerService.authenticate(
                    EPAuthRequest(request)
            )
        } catch (e: Exception) {
            log("authenticate - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/getVisitList",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    ) fun getVisitList (
        request: HttpServletRequest,
        @RequestParam("objectId") objectId: String,
        @RequestParam("objectType") objectType: String

    ): ResponseEntity<String>? {
        return try {

            return externalPartnerService.getVisitList(
                EPAuthRequest(request),
                objectId, objectType
            )

        } catch (e: Exception) {
            log("getVisitList - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/getVisitDetail",
        produces = [MediaType.APPLICATION_JSON_VALUE],
    ) fun getVisitDetail (
        request: HttpServletRequest,
        @RequestParam(ID) id: String,
        ): ResponseEntity<String>? {
        return try {
            return externalPartnerService.getVisitDetail(
                EPAuthRequest(request),
                id
            )

        } catch (e: Exception) {
            log("getVisitDetail - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

}