package com.homefirstindia.rmproserver.model.v1


import com.homefirstindia.rmproserver.utils.DateTimeUtils
import com.homefirstindia.rmproserver.utils.DateTimeUtils.getCurrentDateTimeInIST
import com.homefirstindia.rmproserver.utils.NA
import com.homefirstindia.rmproserver.utils.isNotNullOrNA
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.GenericGenerator
import org.json.JSONArray
import javax.persistence.*


@Entity
@Table(name = "`Partner`")
class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    var id = -1

    var orgName: String = NA
    var orgId: String = NA
    var destination: String = NA
    var leadSource: String = NA
    var leadOwner: String = NA
    var branch: String = NA
    var clientId: String = NA
    var clientSecret: String = NA

    @ColumnDefault("0")
    var isInternal = false

    @Column(columnDefinition = "JSON", name = "servicesAllowed")
    var services: String = NA

    @ColumnDefault("0")
    var isEnabled = false

    @ColumnDefault("0")
    var ipRestricted = false

    @ColumnDefault("0")
    var requiredConsent = false

    @ColumnDefault("1")
    var sessionEnabled = true
    var sessionPasscode: String = NA

    @Column(columnDefinition = "DATETIME")
    var sessionValidDatetime: String = NA

    @Column(columnDefinition = "DATETIME")
    var sessionUpdateDatetime: String = NA

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    private fun servicesAllowed() : ArrayList<String> {

        var servicesAllowed = ArrayList<String>()

        if (services.isNotNullOrNA() && services.startsWith("[")) {
            servicesAllowed = ArrayList()
            val serviceArray = JSONArray(services)
            for (i in 0 until serviceArray.length())
                servicesAllowed.add(serviceArray.getString(i))
        }

        return servicesAllowed

    }

    fun isServiceAllowed(service: String) = servicesAllowed().contains(service)

    fun isSessionValid(): Boolean {

        val currentDateTime = DateTimeUtils.getDateFromDateTimeString(
            getCurrentDateTimeInIST()
        )

        val sessionValidDate = DateTimeUtils.getDateFromDateTimeString(
            sessionValidDatetime
        )

        return currentDateTime.before(sessionValidDate)

    }

    fun shouldIncreaseSessionValidity() : Boolean {

        val validityLeft = DateTimeUtils.getDateDifferenceInMinutes(
            getCurrentDateTimeInIST(),
            sessionValidDatetime
        )

        return validityLeft in 0..14

    }

}

@Entity
@Table(name = "`PartnerLog`")
class PartnerLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    @Column(nullable = false)
    var orgId: String? = null

    var endpoint: String? = null
    var ipAddress: String? = null
    var requestDesc: String? = null
    var requestStatus: String? = null
    var serviceName = NA
    var responseStatus = -1

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var datetime: String = getCurrentDateTimeInIST()

}

@Entity
@Table(name = "`whitelisted_ip`")
class WhitelistedIP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    var id = -1

    @Column(name = "org_id", nullable = false)
    var orgId = NA

    @Column(name = "ip_address", nullable = false)
    var ipAddress = NA

    @ColumnDefault("0")
    @Column(name = "is_active")
    var isActive = false

}

