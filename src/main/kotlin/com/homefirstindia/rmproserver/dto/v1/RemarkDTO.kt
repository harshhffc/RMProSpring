package com.homefirstindia.rmproserver.dto.v1

import com.homefirstindia.rmproserver.utils.*

class RemarkDTO {

    var objectId: String? = null
    var objectName: String? = null
    var remark: String? = null
    var source: String = SOURCE_RM_PRO
    var sfUserId: String? = null
    var ownerName: String? = null

    fun mandatoryFieldsCheck() : LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            objectId.isInvalid() -> localResponse.message = "Invalid  objectId."
            objectName.isInvalid() -> localResponse.message = "Invalid  objectName."
            remark.isInvalid()  -> localResponse.message = "Invalid remark."
            sfUserId.isInvalid() -> localResponse.message = "Invalid sfUserId."
            ownerName.isInvalid() -> localResponse.message = "Invalid ownerName."

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

    fun mandatoryFieldsCheckForGetRemarks() : LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            objectId.isInvalid() -> localResponse.message = "Invalid  objectId."
            objectName.isInvalid() -> localResponse.message = "Invalid  objectName."

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