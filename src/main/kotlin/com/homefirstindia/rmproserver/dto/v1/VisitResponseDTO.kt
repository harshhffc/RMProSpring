package com.homefirstindia.rmproserver.dto.v1

import com.homefirstindia.rmproserver.model.v1.common.Address
import com.homefirstindia.rmproserver.model.v1.visit.ItemCount
import com.homefirstindia.rmproserver.model.v1.visit.Visit
import com.homefirstindia.rmproserver.utils.*
import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvBindByPosition

class LocationDistanceMatrix {

    var id: String? = null
    var originLatitude: Double? = null
    var originLongitude: Double? = null
    var destinationLatitude: Double? = null
    var destinationLongitude: Double? = null
    var travelMode: String? = null
    var distance: Int? = null
    var duration: Int? = null
    var originAddress: String? = null
    var destinationAddress: String? = null
    var success: Boolean = false
    var error: String? = null

}


class LocationDirections {

    var id: String? = null
    var originLatitude: Double? = null
    var originLongitude: Double? = null
    var destinationLatitude: Double? = null
    var destinationLongitude: Double? = null
    var travelMode: String? = null
    var distance: Int? = null
    var duration: Int? = null
    var originAddress: String? = null
    var destinationAddress: String? = null
    var success: Boolean = false
    var error: String? = null

}

class VisitDTO {

    var id: String? = null

    var firstName: String? = null

    var lastName: String? = null
    var mobileNumber: String? = null

    var objectId: String? = null
    var objectType: String? = null

    var travelMode: String? = null

    var reason: String? = null

    var status: String? = null

    var origin: Address? = null

    var actualOrigin: Address? = null

    var destination: Address? = null
    var estimatedDistance: Int? = null
    var estimatedDuration: Int? = null
    var startDatetime: String? = null

    var createDatetime: String? = null

    var sfUserId: String? = null
    var userId: Int? = null
    var username: String? = null

    fun getVisitDTO(visit: Visit) {
        id = visit.id
        status = visit.status
        reason = visit.reason
        travelMode = visit.travelMode
        objectType = visit.objectType
        firstName = visit.firstName
        lastName = visit.lastName
        mobileNumber = visit.mobileNumber
        objectId = visit.objectId
        createDatetime = visit.createDatetime
        startDatetime = visit.startDatetime
        estimatedDistance = visit.estimatedDistance
        estimatedDuration = visit.estimatedDuration
        origin = visit.origin
        actualOrigin = visit.actualOrigin
        destination = visit.destination
        sfUserId = visit.sfUserId
        userId = visit.userId
        username = visit.username
    }

}

class VisitExport() {

    @CsvBindByName(column = "ID", required = true)
    var id: String? = null

    @CsvBindByName(column = "Owner")
    var username: String? = null

    @CsvBindByName(column = "Customer Name", required = true)
    var firstName: String? = null

    @CsvBindByName(column = "Visit Type", required = true)
    var objectType: String? = null

    @CsvBindByName(column = "Visit Object SfId")
    var objectId: String? = null

    @CsvBindByName(column = "Visit Object SfUrl")
    var objectSfUrl: String? = null

    @CsvBindByName(column = "Travel Mode", required = true)
    var travelMode: String? = null

    @CsvBindByName(column = "Remark")
    var remark: String? = null

    @CsvBindByName(column = "Reason", required = true)
    var reason: String? = null

    @CsvBindByName(column = "Result")
    var result: String? = null

    @CsvBindByName(column = "Status", required = true)
    var status: String? = null

    @CsvBindByName(column = "Distance In Km", required = true)
    var estimatedDistance: Double? = null

    @CsvBindByName(column = "Duration In Min", required = true)
    var estimatedDuration: Double? = null

    @CsvBindByName(column = "Start Datetime", required = true)
    var startDatetime: String? = null

    @CsvBindByName(column = "End Datetime")
    var endDatetime: String? = null

    @CsvBindByName(column = "Owner SfId")
    var ownerSfId: String? = null

    @CsvBindByName(column = "PTP Date")
    var ptpDate: String? = null

    @CsvBindByName(column = "Branch")
    var branch: String? = null

    @CsvBindByName(column = "Cluster")
    var cluster: String? = null

    @CsvBindByName(column = "Region")
    var region: String? = null

    constructor(
        id: String?,
        username: String?,
        firstName: String?,
        objectId: String?,
        objectType: String?,
        travelMode: String?,
        remark: String?,
        reason: String?,
        result: String?,
        status: String?,
        estimatedDistance: Int?,
        estimatedDuration: Int?,
        startDatetime: String?,
        endDatetime: String?,
        ownerSfId: String?,
        ptpDate: String?,
        branch: String?,
        cluster: String?,
        region: String?

    ) : this() {
        this.id = id
        this.username = username
        this.firstName = firstName
        this.objectId = objectId
        this.objectType = objectType
        this.travelMode = travelMode
        this.remark = remark
        this.reason = reason
        this.result = result
        this.status = status
        this.estimatedDistance = estimatedDistance?.toDouble()
        this.estimatedDuration = estimatedDuration?.toDouble()
        this.startDatetime = startDatetime
        this.endDatetime = endDatetime
        this.ownerSfId = ownerSfId
        this.ptpDate = ptpDate
        this.branch = branch
        this.cluster = cluster
        this.region = region

    }
}

class VisitDashboardDTO() {
    var status: String? = null
    var count: Long? = null
    var objectType: String? = null

    constructor(
        status: String?,
        count: Long?,
        objectType: String?
    ) : this() {
        this.status = status
        this.count = count
        this.objectType = objectType
    }
}

class VisitResponseDTO() {
    var id: String? = null
    var status: String? = null
    var reason: String? = null
    var createDatetime: String? = null

    constructor(
        id: String?,
        status: String?,
        reason: String?,
        createDatetime: String?
    ) : this() {
        this.id = id
        this.status = status
        this.reason = reason
        this.createDatetime = createDatetime
    }
}

class AdvanceFilter {
    var startDatetime: String? = null
    var endDatetime: String? = null
    var status: String? = null
    var objectType: ArrayList<String>? = null
    var idsType: String? = null
    var selectedIds: ArrayList<String>? = null
    var sfUserId: String? = null

    fun mandatoryFieldsCheck(): LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            startDatetime.isInvalid() -> localResponse.message = "Invalid startDatetime."
            endDatetime.isInvalid() -> localResponse.message = "Invalid endDatetime."
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