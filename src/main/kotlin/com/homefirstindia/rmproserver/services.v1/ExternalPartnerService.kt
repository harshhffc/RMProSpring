package com.homefirstindia.rmproserver.services.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.rmproserver.dto.v1.externalPartner.EPAuthRequest
import com.homefirstindia.rmproserver.helper.v1.PartnerLogHelper
import com.homefirstindia.rmproserver.helper.v1.UserActionStatus
import com.homefirstindia.rmproserver.repository.v1.PartnerRepository
import com.homefirstindia.rmproserver.security.ContextProvide
import com.homefirstindia.rmproserver.utils.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import kotlin.jvm.Throws

@Service
class ExternalPartnerService(
    @Autowired val oneResponse: OneResponse,
    @Autowired val contextProvider: ContextProvide,
    @Autowired val partnerLogHelper: PartnerLogHelper,
    @Autowired val partnerRepository: PartnerRepository,
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val visitService: VisitService
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")
    private fun printLog(value: String) = LoggerUtils.printLog("v1/${this.javaClass.simpleName}.$value")

    @Throws(Exception::class)
    fun authenticate(epAuthRequest: EPAuthRequest): ResponseEntity<String>? {

        val cmsPartner = contextProvider.getPartner(epAuthRequest.orgId)
        cmsPartner ?: return oneResponse.resourceNotFound("No partner found for orgId : ${epAuthRequest.orgId}")

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        val passcodeString = "${getRandomUUID().replace("-", "")}${epAuthRequest.orgId}${System.currentTimeMillis()}"

        cmsPartner.apply {
            sessionPasscode = cryptoUtils.encryptAes(passcodeString)
            updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
            sessionUpdateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
            sessionValidDatetime = DateTimeUtils.getDateTimeByAddingHours(1)
        }

        partnerRepository.save(cmsPartner)

        epLogger
                .setRequestStatus(UserActionStatus.SUCCESS)
                .log()

        printLog("authenticate - cmsPartner.sessionPasscode : ${ cmsPartner.sessionPasscode}")

        return oneResponse.getSuccessResponse(
                JSONObject()
                        .put(SESSION_PASSCODE, cmsPartner.sessionPasscode)
                        .put(VALID_UPTO, cmsPartner.sessionValidDatetime)
        )

    }

    @Throws(Exception::class)
    fun getVisitList(
        epAuthRequest: EPAuthRequest,
        objectId: String,
        objectType: String,
    ): ResponseEntity<String>?{

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        val visitActivityResponse = visitService.getAllVisitByObjectDetail(objectId, objectType)

        epLogger
            .setRequestStatus(
                if (visitActivityResponse.statusCode == HttpStatus.OK) UserActionStatus.SUCCESS
                else UserActionStatus.FAILURE
            )
            .log()

        return visitActivityResponse
    }

    @Throws(Exception::class)
    fun getVisitDetail(
        epAuthRequest: EPAuthRequest,
        id: String,
    ): ResponseEntity<String>?{

        val cmsPartner = contextProvider.getPartner(epAuthRequest.orgId)
        cmsPartner ?: return oneResponse.resourceNotFound("No partner found for orgId : ${epAuthRequest.orgId}")

        val epLogger = partnerLogHelper.Builder(epAuthRequest)

        val visitActivityResponse = visitService.getDetail(id)

        epLogger
            .setRequestStatus(
                if (visitActivityResponse.statusCode == HttpStatus.OK) UserActionStatus.SUCCESS
                else UserActionStatus.FAILURE
            )
            .log()

        return visitActivityResponse
    }

}