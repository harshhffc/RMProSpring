package com.homefirstindia.rmproserver.helper.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.rmproserver.dto.v1.externalPartner.EPAuthRequest
import com.homefirstindia.rmproserver.model.v1.PartnerLog
import com.homefirstindia.rmproserver.model.v1.user.UserLog
import com.homefirstindia.rmproserver.repository.v1.PartnerLogRepository
import com.homefirstindia.rmproserver.repository.v1.UserLogRepository
import com.homefirstindia.rmproserver.utils.MyObject
import com.homefirstindia.rmproserver.utils.THREAD_POOL_TASK_EXECUTOR
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component

@EnableAsync
@Component
class UserLogHelper(
    @Autowired private val userLogRepository: UserLogRepository
) {

    inner class Builder {

        private var userLog = UserLog()

        fun setUserId(id: Int): Builder {
            userLog.userId = id.toString()
            return this
        }

        fun setUserAction(userAction: UserAction): Builder {
            userLog.userAction = userAction.value
            return this
        }

        fun setActionDesc(actionDesc: String?): Builder {
            userLog.actionDesc = actionDesc
            return this
        }

        fun setObjectId(objectId: String?): Builder {
            userLog.objectId = objectId
            return this
        }

        fun setObjectName(objectName: MyObject): Builder {
            userLog.objectName = objectName.value
            return this
        }

        fun setIpAddress(ipAddress: String?): Builder {
            userLog.ipAddress = ipAddress
            return this
        }

        fun setDeviceId(deviceId: String?): Builder {
            userLog.deviceId = deviceId
            return this
        }

        fun setDeviceType(deviceType: String?): Builder {
            userLog.deviceType = deviceType
            return this
        }

        fun setDeviceName(deviceName: String?): Builder {
            userLog.deviceName = deviceName
            return this
        }

        fun setMethodName(methodName: String?): Builder {
            userLog.methodName = methodName
            return this
        }

        fun setRequestStatus(requestStatus: UserActionStatus): Builder {
            userLog.requestStatus = requestStatus.name
            return this
        }

        @Async(THREAD_POOL_TASK_EXECUTOR)
        fun log() {
            userLogRepository.save(userLog)
        }

    }

}

enum class
UserAction(val value: String) {
    ADD("ADD"),
    VIEW("VIEW"),
    GET("GET"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    PASSWORD_FORGOT("PASSWORD_FORGOT"),
    PASSWORD_RESET("PASSWORD_RESET"),
    REFRESH_TOKEN("REFRESH_TOKEN"),
}

enum class UserActionStatus {
    SUCCESS, FAILURE
}

@EnableAsync
@Component
class PartnerLogHelper(
    @Autowired private val partnerLogRepository: PartnerLogRepository
) {

    inner class Builder() {

        constructor(epAuthRequest: EPAuthRequest): this() {

            epAuthRequest.run {
                epLog.orgId = orgId
                epLog.endpoint = requestUri
                epLog.ipAddress = ipAddress
            }

        }

        private var epLog = PartnerLog()

        fun setOrgId(value: String?) : Builder {
            epLog.orgId = value
            return this
        }

        fun setEndpoint(value: String?) : Builder {
            epLog.endpoint = value
            return this
        }

        fun setRequestDesc(value: String?) : Builder {
            epLog.requestDesc = value
            return this
        }

        fun setRequestStatus(value: UserActionStatus) : Builder {
            epLog.requestStatus = value.name
            return this
        }


        fun setIPAddress(value: String?) : Builder {
            epLog.ipAddress = value
            return this
        }

        @Async(THREAD_POOL_TASK_EXECUTOR)
        fun log() {
            partnerLogRepository.save(epLog)
        }

    }

}