package com.homefirstindia.rmproserver.model.v1.visit

import com.homefirstindia.rmproserver.dto.v1.AdvanceFilter
import com.homefirstindia.rmproserver.model.v1.common.Address
import com.homefirstindia.rmproserver.model.v1.common.Attachment
import com.homefirstindia.rmproserver.model.v1.user.User
import com.homefirstindia.rmproserver.utils.*
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*
import kotlin.jvm.Transient

@Entity
@Table(name = "`Visit`")
class Visit {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    @Column(updatable = false, nullable = false)
    var userId: Int? = null
    var username: String? = null

    @Column(updatable = false, nullable = false)
    var sfUserId: String? = null


    var objectId: String? = null
    var objectType: String? = null

    var firstName: String? = null
    var lastName: String? = null
    var mobileNumber: String? = null

    var status: String = PENDING

    var reason: String? = null
    var result: String? = null
    var remark: String? = null

    @Column(columnDefinition = "DATETIME")
    var startDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME", nullable = true)
    var endDatetime: String? = null

    @Column(columnDefinition = "DATETIME", nullable = true)
    var estimatedDatetime: String? = null

    var estimatedDuration: Int? = null
    var actualDuration: Int? = null

    var estimatedDistance: Int? = null
    var actualDistance: Int? = null

    var travelMode: String? = null

    var pdcNumber: String? = null
    var pdcAmount: Double? = null
    var pdcDeposited: Boolean? = null

    @Column(columnDefinition = "DATETIME", nullable = true)
    var ptpDate: String? = null

    var anotherVisitRequired: Boolean? = null

    var parentId: String? = null

    var originChange: Boolean? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "originAddressId", referencedColumnName = "id")
    var origin: Address? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "actualOriginAddressId", referencedColumnName = "id")
    var actualOrigin: Address? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "destinationAddressId", referencedColumnName = "id")
    var destination: Address? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "actualDestinationAddressId", referencedColumnName = "id")
    var actualDestination: Address? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "routeAttachmentId", referencedColumnName = "id")
    var routeAttachment: Attachment? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "onsiteAttachmentId", referencedColumnName = "id")
    var onsiteAttachment: Attachment? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME", nullable = false)
    var updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    @Transient
    var owner: User? = null

    fun updateUserInfo(user: User) {
        userId = user.id
        sfUserId = user.sfUserId
        username = user.displayName
    }

    fun updateFields(nVisit: Visit) {

        result = nVisit.result
        remark = nVisit.remark
        status = COMPLETED
        pdcDeposited = nVisit.pdcDeposited
        pdcNumber = nVisit.pdcNumber
        pdcAmount = nVisit.pdcAmount
        ptpDate = nVisit.ptpDate
        endDatetime = DateTimeUtils.getCurrentDateTimeInIST()
        actualDistance = nVisit.actualDistance
        actualDuration = nVisit.actualDuration
        actualDestination = nVisit.actualDestination
        anotherVisitRequired = nVisit.anotherVisitRequired
        updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    }

    fun mandatoryFieldsCheck(): LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        objectType?.let {

            val objectType = EnObjectType[objectType!!]

            objectType ?: run {
                localResponse.message = "Invalid objectType"
                return localResponse
            }

        } ?: run {
            localResponse.message = "Invalid objectType"
            return localResponse
        }

        if (objectType != EnObjectType.CONNECTOR.value) {
            if (objectId.isInvalid()) {
                localResponse.message = "Invalid objectId"
                return localResponse
            }
        }

        when {
            userId.isInvalid() -> localResponse.message = "Invalid userId"
            sfUserId.isInvalid() -> localResponse.message = "Invalid sfUserId"
            firstName.isInvalid() -> localResponse.message = "Invalid name"
            reason.isInvalid() -> localResponse.message = "Invalid reason"
            travelMode.isInvalid() -> localResponse.message = "Invalid travel mode"
            !actualOrigin?.isPartiallyValid()!! -> localResponse.message = "Invalid actual origin address"
            !destination?.isPartiallyValid()!! -> localResponse.message = "Invalid destination address"

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

    fun mandatoryFieldsCheckForUpdate(): LocalResponse {

        val localResponse = LocalResponse()
            .setError(Errors.INVALID_DATA.value)
            .setAction(Actions.FIX_RETRY.value)

        when {
            id.isInvalid() -> localResponse.message = "Invalid visit id"
            result.isInvalid() -> localResponse.message = "Invalid result"
            !actualDestination?.isPartiallyValid()!! -> localResponse.message = "Invalid actual destination address"

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

class Dashlet(
    var key: String? = null,
    var name: String? = null,
    var count: Int? = null,
    var pendingCount: Int? = null,
    var completedCount: Int? = null
) {

    constructor(key: String?, name: String?, count: ItemCount?) : this()

    fun getDashletRoleWise(
        role: String, zoneCount: Int, regionCount: Int,
        clusterCount: Int, branchCount: Int, userCount: Int
    ): ArrayList<Dashlet> {

        val dashboardData = ArrayList<Dashlet>()

        when (role) {

            EnRole.HO_USER.value -> {
                dashboardData.add(Dashlet(EnDashlet.ZONE.value, EnDashlet.ZONE.displayName, zoneCount))
                dashboardData.add(Dashlet(EnDashlet.REGION.value, EnDashlet.REGION.displayName, regionCount))
                dashboardData.add(Dashlet(EnDashlet.CLUSTER.value, EnDashlet.CLUSTER.displayName, clusterCount))
                dashboardData.add(Dashlet(EnDashlet.BRANCH.value, EnDashlet.BRANCH.displayName, branchCount))
                dashboardData.add(Dashlet(EnDashlet.USER.value, EnDashlet.USER.displayName, userCount))
            }

            EnRole.ZM.value -> {
                dashboardData.add(Dashlet(EnDashlet.REGION.value, EnDashlet.REGION.displayName, regionCount))
                dashboardData.add(Dashlet(EnDashlet.CLUSTER.value, EnDashlet.CLUSTER.displayName, clusterCount))
                dashboardData.add(Dashlet(EnDashlet.BRANCH.value, EnDashlet.BRANCH.displayName, branchCount))
                dashboardData.add(Dashlet(EnDashlet.USER.value, EnDashlet.USER.displayName, userCount))
            }

            EnRole.RGM.value -> {
                dashboardData.add(Dashlet(EnDashlet.CLUSTER.value, EnDashlet.CLUSTER.displayName, clusterCount))
                dashboardData.add(Dashlet(EnDashlet.BRANCH.value, EnDashlet.BRANCH.displayName, branchCount))
                dashboardData.add(Dashlet(EnDashlet.USER.value, EnDashlet.USER.displayName, userCount))
            }

            EnRole.CM.value -> {
                dashboardData.add(Dashlet(EnDashlet.BRANCH.value, EnDashlet.BRANCH.displayName, branchCount))
                dashboardData.add(Dashlet(EnDashlet.USER.value, EnDashlet.USER.displayName, userCount))
            }

            EnRole.BM.value -> {
                dashboardData.add(Dashlet(EnDashlet.USER.value, EnDashlet.USER.displayName, userCount))
            }

            else -> {
                println("No role match found")
            }
        }

        return dashboardData
    }

    fun getDashletZRCBUWise(
        advanceFilter: AdvanceFilter, regionCount: Int,
        clusterCount: Int, branchCount: Int, userCount: Int
    ): ArrayList<Dashlet> {

        val dashboardData = ArrayList<Dashlet>()

        when (advanceFilter.idsType) {

            EnFilterType.ZONE.key -> {
                dashboardData.add(Dashlet(EnDashlet.REGION.value, EnDashlet.REGION.displayName, regionCount))
                dashboardData.add(Dashlet(EnDashlet.CLUSTER.value, EnDashlet.CLUSTER.displayName, clusterCount))
                dashboardData.add(Dashlet(EnDashlet.BRANCH.value, EnDashlet.BRANCH.displayName, branchCount))
                dashboardData.add(Dashlet(EnDashlet.USER.value, EnDashlet.USER.displayName, userCount))
            }

            EnFilterType.REGION.key -> {
                dashboardData.add(Dashlet(EnDashlet.CLUSTER.value, EnDashlet.CLUSTER.displayName, clusterCount))
                dashboardData.add(Dashlet(EnDashlet.BRANCH.value, EnDashlet.BRANCH.displayName, branchCount))
                dashboardData.add(Dashlet(EnDashlet.USER.value, EnDashlet.USER.displayName, userCount))
            }

            EnFilterType.CLUSTER.key -> {
                dashboardData.add(Dashlet(EnDashlet.BRANCH.value, EnDashlet.BRANCH.displayName, branchCount))
                dashboardData.add(Dashlet(EnDashlet.USER.value, EnDashlet.USER.displayName, userCount))
            }

            EnFilterType.BRANCH.key -> {
                dashboardData.add(Dashlet(EnDashlet.USER.value, EnDashlet.USER.displayName, userCount))
            }
        }

        return dashboardData
    }
}

class ItemCount(sfId: String, var total: Long?) {
    var sfId: String? = sfId
}

