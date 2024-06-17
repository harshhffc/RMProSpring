package com.homefirstindia.rmproserver.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.rmproserver.model.v1.CallLog
import com.homefirstindia.rmproserver.model.v1.CallLogAdvanceFilter
import com.homefirstindia.rmproserver.services.v1.CommunicationService
import com.homefirstindia.rmproserver.utils.ID
import com.homefirstindia.rmproserver.utils.LoggerUtils
import com.homefirstindia.rmproserver.utils.OneResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("communication/v1")
class CommunicationController(
    @Autowired val oneResponse: OneResponse,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val communicationService: CommunicationService
) {

    private fun logMethod(value: String) = LoggerUtils.logMethodCall("/communication/v1/$value")

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    @PostMapping(
        "/CallLog.getAll",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comGetAllCallLogs(
        @RequestHeader userSourceId: Int,
        @RequestBody advanceFilterObject: CallLogAdvanceFilter,
        page: Pageable
    ): ResponseEntity<String>? {

        logMethod("comGetAllCallLogs")

        return try {
            communicationService.getAllCallLogs(userSourceId, advanceFilterObject, page)
        } catch (e: Exception) {
            log("comGetAllCallLogs - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/CallLog.getDetail/{id}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun comGetCallLogDetail(
        @RequestHeader userSourceId: Int,
        @PathVariable(ID) id: String
    ): ResponseEntity<String>? {

        logMethod("comGetCallLogDetail")

        return try {
            communicationService.getCallLogDetail(userSourceId, id)
        } catch (e: Exception) {
            log("comGetCallLogDetail - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/CallLog.addRemark",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun addRemarkCallLog(
        @RequestHeader userSourceId: Int,
        @RequestBody callLog: CallLog
    ): ResponseEntity<String>? {

        logMethod("addCallLogRemark")

        return try {
            communicationService.addCallLogRemark(userSourceId, callLog)
        } catch (e: Exception) {
            log("addRemarkCallLog - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }
}