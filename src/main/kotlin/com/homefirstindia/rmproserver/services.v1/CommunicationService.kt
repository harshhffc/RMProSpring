package com.homefirstindia.rmproserver.services.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.rmproserver.helper.v1.UserAction
import com.homefirstindia.rmproserver.helper.v1.UserActionStatus
import com.homefirstindia.rmproserver.helper.v1.UserLogHelper
import com.homefirstindia.rmproserver.model.v1.CallLog
import com.homefirstindia.rmproserver.model.v1.CallLogAdvanceFilter
import com.homefirstindia.rmproserver.networking.v1.HFONetworkClient
import com.homefirstindia.rmproserver.repository.v1.UserRepository
import com.homefirstindia.rmproserver.utils.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class CommunicationService(
    @Autowired val oneResponse: OneResponse,
    @Autowired val hfoNetworkClient: HFONetworkClient,
    @Autowired val userLogHelper: UserLogHelper,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val userRepository: UserRepository
) {
    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")
    private fun methodName(value: String): String = "v1/${this.javaClass.simpleName}.$value"

    @Throws(Exception::class)
    fun getAllCallLogs(
        userId: Int,
        advanceFilterObject: CallLogAdvanceFilter,
        page: Pageable,
    ): ResponseEntity<String>? {

        val eUser = userRepository.findByUserId(userId)!!

        val userLogger = userLogHelper.Builder()
            .setUserId(eUser.id)
            .setUserAction(UserAction.GET)
            .setMethodName(methodName("getAllCallLogs"))

        advanceFilterObject.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                val msg = it.message
                log("getAllCallLogs - $msg")
                userLogger.setActionDesc(it.message).setRequestStatus(UserActionStatus.FAILURE).log()
                return oneResponse.invalidData(msg)
            }
        }

        val url = "${HFONetworkClient.Endpoints.CALL_LOG_ADVANCED_FILTER.value}?size=${page.pageSize}&page=${page.pageNumber}"

        val dmResponse = hfoNetworkClient.post(
            url,
            JSONObject(objectMapper.writeValueAsString(advanceFilterObject))
        )

        if (!dmResponse.isSuccess) {
            log("getAllCallLogs: ${dmResponse.message}")
            userLogger.setRequestStatus(UserActionStatus.FAILURE).log()
            return oneResponse.getFailureResponse(dmResponse.toJson())
        }

        userLogger
            .setRequestStatus(UserActionStatus.SUCCESS)
            .setActionDesc("Fetched call log on filter successfully!")
            .log()

        return oneResponse.getSuccessResponse(
            JSONObject(dmResponse.response)
        )

    }

    @Throws(Exception::class)
    fun getCallLogDetail(
        userId: Int,
        id: String
    ): ResponseEntity<String>? {

        val eUser = userRepository.findByUserId(userId)!!

        val userLogger = userLogHelper.Builder()
            .setUserId(eUser.id)
            .setUserAction(UserAction.GET)
            .setMethodName(methodName("getAllCallDetail"))

        if (id.isInvalid()) {
            val msg = "Invalid call log Id"
            log("getCallLogDetail - $msg")
            userLogger.setActionDesc(msg).setRequestStatus(UserActionStatus.FAILURE).log()
            return oneResponse.invalidData(msg)
        }

        val dmResponse = hfoNetworkClient.get(
            HFONetworkClient.Endpoints.CALL_LOG_DETAIL,
            "?id=$id"
        )

        if (!dmResponse.isSuccess) {
            log("getAllCallDetail: ${dmResponse.message}")
            userLogger.setActionDesc(dmResponse.message).setRequestStatus(UserActionStatus.FAILURE).log()
            return oneResponse.getFailureResponse(dmResponse.toJson())
        }

        userLogger
            .setRequestStatus(UserActionStatus.SUCCESS)
            .setActionDesc("Call log details fetched successfully!")
            .log()

        return oneResponse.getSuccessResponse(
            JSONObject(dmResponse.response)
        )

    }

    @Throws(Exception::class)
    fun addCallLogRemark(
        userId: Int,
        callLog: CallLog
    ): ResponseEntity<String>? {

        val eUser = userRepository.findByUserId(userId)!!

        val userLogger = userLogHelper.Builder()
            .setUserId(eUser.id)
            .setUserAction(UserAction.GET)
            .setMethodName(methodName("addCallLogRemark"))

        callLog.mandatoryFieldsCheckForRemark().let {
            if (!it.isSuccess) {
                val msg = it.message
                log("addCallLogRemark - $msg")
                userLogger.setActionDesc(it.message).setRequestStatus(UserActionStatus.FAILURE).log()
                return oneResponse.invalidData(msg)
            }
        }

        if (eUser.sfUserId != callLog.userId) {
            val msg = "You're not allow to add remark"
            userLogger.setActionDesc(msg).setRequestStatus(UserActionStatus.FAILURE).log()
            return oneResponse.operationFailedResponse(msg)
        }

        val dmResponse = hfoNetworkClient.post(
            HFONetworkClient.Endpoints.CALL_LOG_REMARK, JSONObject(callLog)
        )

        if (!dmResponse.isSuccess) {
            log("addCallLogRemark: ${dmResponse.message}")
            userLogger.setActionDesc(dmResponse.message).setRequestStatus(UserActionStatus.FAILURE).log()
            userLogger.setRequestStatus(UserActionStatus.FAILURE).log()
            return oneResponse.getFailureResponse(dmResponse.toJson())
        }

        userLogger
            .setRequestStatus(UserActionStatus.SUCCESS)
            .setActionDesc("Remark added successfully!")
            .log()

        return oneResponse.getSuccessResponse(
            JSONObject(dmResponse.response)
        )

    }
}