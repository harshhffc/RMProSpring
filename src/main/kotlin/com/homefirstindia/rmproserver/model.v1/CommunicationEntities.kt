package com.homefirstindia.rmproserver.model.v1

import com.homefirstindia.rmproserver.utils.*

class CallLog {
    var id: String? = null
    var userId: String? = null
    var remark: String? = null

    fun mandatoryFieldsCheckForRemark(): LocalResponse {

        val lResponse = LocalResponse()
        lResponse.action = Actions.FIX_RETRY.value
        lResponse.error = Errors.INVALID_DATA.value

        when {
            id.isInvalid() -> lResponse.message = "Invalid call log id."
            userId.isInvalid() -> lResponse.message = "Invalid user id."
            remark.isInvalid() -> lResponse.message = "Invalid remark."

            else -> {

                lResponse.apply {
                    message = "Request is valid"
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }

        }
        return lResponse
    }
}

class CallLogAdvanceFilter {

    var groupConditionOp: String? = null
    var conditions: List<RowCondition> = listOf()

    fun mandatoryFieldsCheck(): LocalResponse {
        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)


        when {
            groupConditionOp.isInvalid() -> localResponse.message = "Invalid Group Condition"
            conditions.isEmpty() -> localResponse.message = "Invalid Conditions"
            else -> {
                localResponse.apply {
                    message = NA
                    error = NA
                    action = NA
                    isSuccess = true
                }
            }
        }

        return localResponse

    }
}

class RowCondition {
    var type: String? = null
    var lso = NA
    var op: String? = null
    var rso: ArrayList<String>? = null

    fun mandatoryFieldCheck(): LocalResponse {
        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            type.isNotNullOrNA() || type.isInvalid() -> localResponse.message = "Invalid Type $type"
            lso.isNotNullOrNA() || lso.isInvalid() -> localResponse.message = "Invalid Left Side Operator $lso"
            op.isNotNullOrNA() || op.isInvalid() -> localResponse.message = "Invalid Operator $op"
            rso.isNullOrEmpty() -> localResponse.message = "Invalid Right Side Operator $rso"

        }

        return localResponse
    }
}