package com.homefirstindia.rmproserver.model.v1

import com.homefirstindia.rmproserver.utils.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "`Creds`")
class Creds {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    @Column(nullable = false)
    var partnerName: String? = null
    var credType: String? = null
    var username: String? = null
    var password: String? = null
    var memberId: String? = null
    var memberPasscode: String? = null
    var apiUrl: String? = null
    var salt: String? = null
    var apiKey: String? = null

    @ColumnDefault("1")
    var isValid = true

    @ColumnDefault("0")
    var isEncrypted = false

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    fun mandatoryFieldsCheck(): LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            credType.isInvalid() -> localResponse.message = "Invalid credType."
            partnerName.isInvalid() -> localResponse.message = "Invalid partnerName."
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