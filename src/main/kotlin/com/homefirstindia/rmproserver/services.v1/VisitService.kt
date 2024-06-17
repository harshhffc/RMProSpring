package com.homefirstindia.rmproserver.services.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.rmproserver.dto.v1.*
import com.homefirstindia.rmproserver.helper.v1.*
import com.homefirstindia.rmproserver.manager.v1.AmazonClient
import com.homefirstindia.rmproserver.manager.v1.EnS3BucketPath
import com.homefirstindia.rmproserver.model.v1.common.Address
import com.homefirstindia.rmproserver.model.v1.visit.Dashlet
import com.homefirstindia.rmproserver.model.v1.visit.Visit
import com.homefirstindia.rmproserver.networking.v1.HFONetworkClient
import com.homefirstindia.rmproserver.repository.v1.*
import com.homefirstindia.rmproserver.security.AppProperty
import com.homefirstindia.rmproserver.utils.*
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URLEncoder
import java.util.stream.Collectors.*


@Service
class VisitService(
    @Autowired val oneResponse: OneResponse,
    @Autowired val visitRepository: VisitRepository,
    @Autowired val appProperty: AppProperty,
    @Autowired val amazonClient: AmazonClient,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val hfoNetworkClient: HFONetworkClient,
    @Autowired val resultRepository: ResultRepository,
    @Autowired val reasonRepository: ReasonRepository,
    @Autowired val userRepository: UserRepository,
    @Autowired val userMapMasterRepository: UserMapMasterRepository,
    @Autowired val userLogHelper: UserLogHelper,
    @Autowired val documentHelper: DocumentHelper,
    @Autowired val visitProcessHelper: VisitProcessHelper,
    @Autowired val addressRepository: AddressRepository,
    @Autowired val userProcessHelper: UserProcessHelper
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    private fun methodName(value: String): String = "v1/${this.javaClass.simpleName}.$value"

    @Throws(Exception::class)
    fun getMetaData(): ResponseEntity<String> {

        val allReason = reasonRepository.findAll()
        val allResult = resultRepository.findAll()

        return oneResponse.getSuccessResponse(
            JSONObject()
                .put("reason", allReason)
                .put("result", allResult)
        )

    }

    @Throws(Exception::class)
    fun addVisit(
        userId: Int,
        file: MultipartFile?,
        nVisit: Visit
    ): ResponseEntity<String> {

        val eUser = userRepository.findByUserId(userId)!!

        val userLogger = userLogHelper.Builder()
            .setUserId(eUser.id)
            .setObjectName(MyObject.VISIT)
            .setUserAction(UserAction.ADD)
            .setMethodName(methodName("addVisit"))

        nVisit.updateUserInfo(eUser)

        nVisit.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                userLogger
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setActionDesc(it.message)
                    .setMethodName(methodName("addVisit"))
                    .log()
                log("addVisit - ${it.message}")
                return oneResponse.invalidData(it.message)
            }
        }

        visitRepository.save(nVisit)

        file?.let {

            val fileName = "${eUser.id}_${System.currentTimeMillis()}_route.${
                file.contentType!!
                    .substring(file.contentType!!.lastIndexOf("/") + 1)
            }"

            documentHelper.uploadAttachment(fileName, file, EnS3BucketPath.VISIT)?.let {
                it.updateObjectDetails(nVisit.id!!, EnMyObject.VISIT.value)
                nVisit.routeAttachment = it
                visitRepository.save(nVisit)
            } ?: run {
                val msg = "Failed to upload route attachment"
                log("addVisit - Failed to upload route attachment | Visit Id: ${nVisit.id}")
                userLogger
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setActionDesc(msg)
                    .log()
            }

        }

        userLogger
            .setObjectId(nVisit.id)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .setActionDesc("Visit added successfully")
            .log()

        return oneResponse.getSuccessResponse(
            JSONObject()
                .put(MESSAGE, "Visit added successfully")
                .put("visit", JSONObject(objectMapper.writeValueAsString(nVisit)))
        )

    }

    @Throws(Exception::class)
    fun updateVisit(userId: Int, file: MultipartFile?, nVisit: Visit): ResponseEntity<String> {

        val eUser = userRepository.findByUserId(userId)!!

        val userLogger = userLogHelper.Builder()
            .setUserId(eUser.id)
            .setObjectName(MyObject.VISIT)
            .setUserAction(UserAction.UPDATE)
            .setMethodName(methodName("updateVisit"))

        nVisit.mandatoryFieldsCheckForUpdate().let {
            if (!it.isSuccess) {
                userLogger
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setActionDesc(it.message)
                    .setMethodName(methodName("updateVisit"))
                    .log()
                log("updateVisit - ${it.message}")
                return oneResponse.invalidData(it.message)
            }
        }

        val eVisit = visitRepository.findById(nVisit.id!!)
            ?: return oneResponse.resourceNotFound("No visit found")

        eVisit.updateFields(nVisit)
        visitRepository.save(eVisit)

        file?.let {

            val fileName = "${eUser.id}_${System.currentTimeMillis()}_onsite.${
                file.contentType!!
                    .substring(file.contentType!!.lastIndexOf("/") + 1)
            }"

            documentHelper.uploadAttachment(fileName, file, EnS3BucketPath.VISIT)?.let {
                it.updateObjectDetails(eVisit.id!!, EnMyObject.VISIT.value)
                eVisit.onsiteAttachment = it
                visitRepository.save(eVisit)
            } ?: run {
                val msg = "Failed to upload onsite attachment"
                log("updateVisit - Failed to upload onsite attachment | Visit Id: ${nVisit.id}")
                userLogger
                    .setRequestStatus(UserActionStatus.FAILURE)
                    .setActionDesc(msg)
                    .log()
            }

        }

        if (eVisit.objectType == EnObjectType.COLLECTION.value) {

            val remarkDTO = RemarkDTO().apply {

                objectId = eVisit.objectId
                objectName = eVisit.objectType

                remark = "Collection visit completed\n" +
                        "Reason: ${eVisit.reason}\n" +
                        "Result: ${eVisit.result}\n" +
                        "Remark: ${eVisit.remark}\n" +
                        (eVisit.ptpDate?.let {
                            "PTP Date: ${
                                DateTimeUtils.getStringFromDateTimeString(
                                    it,
                                    DateTimeFormat.yyyy_MM_dd_HH_mm_ss,
                                    DateTimeFormat.d_MMM_yyyy
                                )
                            }"
                        } ?: "")

                ownerName = eVisit.username
                sfUserId = eVisit.sfUserId

            }

            remarkDTO.remark = URLEncoder.encode(remarkDTO.remark, "UTF-8")

            val lResponse = userProcessHelper.addRemark(remarkDTO)

            if (!lResponse.isSuccess)
                log("addRemark - Fail to add remark for objectId: ${remarkDTO.objectId} | CMS response: ${lResponse.message}")

        }

        userLogger
            .setObjectId(nVisit.id)
            .setRequestStatus(UserActionStatus.SUCCESS)
            .setActionDesc("Visit updated successfully")
            .log()

        return oneResponse.getSuccessResponse(
            JSONObject()
                .put(MESSAGE, "Visit updated successfully")
                .put("visit", JSONObject(objectMapper.writeValueAsString(eVisit)))
        )

    }

    @Throws(Exception::class)
    fun getDetail(visitId: String): ResponseEntity<String> {

        val eVisit = visitRepository.findById(visitId)

        eVisit ?: run {
            log("getDetail - No visit found for id: $visitId")
            return oneResponse.resourceNotFound("Visit not found")
        }

        eVisit.owner = userRepository.findBySfUserId(eVisit.sfUserId!!)

        eVisit.onsiteAttachment?.let { attachment ->
            amazonClient.getPublicURL(
                attachment.fileName!!, EnS3BucketPath.VISIT, 10
            )?.let {
                attachment.publicUrl = it
            }
        }

        eVisit.routeAttachment?.let { attachment ->
            amazonClient.getPublicURL(
                attachment.fileName!!, EnS3BucketPath.VISIT, 10
            )?.let {
                attachment.publicUrl = it
            }
        }

        return oneResponse.getSuccessResponse(JSONObject().apply {
            put("visit", JSONObject(objectMapper.writeValueAsString(eVisit)))
        })

    }

    @Throws(Exception::class)
    fun getDashboardData(
        userId: Int,
        advanceFilter: AdvanceFilter
    ): ResponseEntity<String> {

        advanceFilter.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                log("getDashboardData - ${it.message}")
                return oneResponse.invalidData(it.message)
            }
        }

        var filterApplied = false

        var childSfUserIds = ArrayList<String>()

        advanceFilter.selectedIds?.let {
            if (it.isNotEmpty()) {
                childSfUserIds = visitProcessHelper.fetchRCBUWiseChildSfUserId(userId, advanceFilter)
                filterApplied = true
            } else {
                childSfUserIds = visitProcessHelper.fetchChildSfUserIdRoleWise(advanceFilter.sfUserId!!)
            }
        } ?: run {
            childSfUserIds = visitProcessHelper.fetchChildSfUserIdRoleWise(advanceFilter.sfUserId!!)
        }

        val zoneCount = userMapMasterRepository.getTotalZones(childSfUserIds)?.size ?: run { 0 }
        val regionCount = userMapMasterRepository.getTotalRegions(childSfUserIds)?.size ?: run { 0 }
        val clusterCount = userMapMasterRepository.getTotalClusters(childSfUserIds)?.size ?: run { 0 }
        val branchCount = userMapMasterRepository.getTotalBranches(childSfUserIds)?.size ?: run { 0 }

        val userCount = if (!filterApplied) childSfUserIds.size - 1 else childSfUserIds.size

        val visitStats = visitRepository.findVisitStatsBetweenDateBySfUserIds(
            childSfUserIds,
            advanceFilter.startDatetime, advanceFilter.endDatetime
        )

        var pendingCount = 0
        var completedCount = 0
        var connectorCount = 0
        var collectionCount = 0
        var leadCount = 0
        var opportunityCount = 0
        var branchVisitCount = 0

        visitStats?.forEach { vs ->
            vs.count?.let { c ->
                if (vs.status == EnVisitStatus.PENDING.value) pendingCount += c.toInt()
                else completedCount += c.toInt()

                when (vs.objectType) {
                    EnObjectType.CONNECTOR.value -> connectorCount += c.toInt()
                    EnObjectType.COLLECTION.value -> collectionCount += c.toInt()
                    EnObjectType.LEAD.value -> leadCount += c.toInt()
                    EnObjectType.OPPORTUNITY.value -> opportunityCount += c.toInt()
                    EnObjectType.BRANCH_VISIT.value -> branchVisitCount += c.toInt()
                }
            }
        }

        val dashboardData = ArrayList<Dashlet>()

        dashboardData.add(Dashlet().apply {
            this.name = EnDashlet.ALL.displayName
            this.key = EnDashlet.ALL.value
            this.pendingCount = pendingCount
            this.completedCount = completedCount
            this.count = pendingCount + completedCount
        })

        var userRole = NA
        if (!filterApplied) {

            userRepository.findBySfUserId(advanceFilter.sfUserId!!)?.let { eUser ->
                eUser.roles?.first().let {
                    it?.name
                }
            }?.let { role ->
                userRole = role
                dashboardData.addAll(
                    Dashlet().getDashletRoleWise(
                        role, zoneCount, regionCount, clusterCount,
                        branchCount, userCount
                    )
                )
            }

        } else {
            dashboardData.addAll(
                Dashlet().getDashletZRCBUWise(
                    advanceFilter, regionCount, clusterCount,
                    branchCount, userCount
                )
            )
        }

        dashboardData.add(Dashlet(EnDashlet.COLLECTION.value, EnDashlet.COLLECTION.displayName, collectionCount))
        dashboardData.add(Dashlet(EnDashlet.CONNECTOR.value, EnDashlet.CONNECTOR.displayName, connectorCount))
        dashboardData.add(Dashlet(EnDashlet.LEAD.value, EnDashlet.LEAD.displayName, leadCount))
        dashboardData.add(Dashlet(EnDashlet.OPPORTUNITY.value, EnDashlet.OPPORTUNITY.displayName, opportunityCount))

        if (userRole != EnRole.BM.value && userRole != EnRole.RM.value && userRole != EnRole.CSM.value)
            dashboardData.add(
                Dashlet(
                    EnDashlet.BRANCH_VISIT.value,
                    EnDashlet.BRANCH_VISIT.displayName,
                    branchVisitCount
                )
            )

        if (!filterApplied) {
            if (userRole != EnRole.HO_USER.value)
                dashboardData.add(Dashlet(EnDashlet.MY_VIEW.value, EnDashlet.MY_VIEW.displayName))
        } else {
            if (advanceFilter.idsType == EnFilterType.USER.key) {
                if (advanceFilter.selectedIds?.size == 1) {
                    dashboardData.add(Dashlet(EnDashlet.MY_VIEW.value, EnDashlet.MY_VIEW.displayName))
                }
            }
        }

        return oneResponse.getSuccessResponse(
            JSONObject().put(
                "visitStats",
                JSONArray(objectMapper.writeValueAsString(dashboardData)),
            )
        )
    }

    @Throws(Exception::class)
    fun getVisits(
        userId: Int,
        advanceFilter: AdvanceFilter,
        pageable: Pageable
    ): ResponseEntity<String> {

        advanceFilter.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                log("getVisits - ${it.message}")
                return oneResponse.invalidData(it.message)
            }
        }

        var childSfUserIds = ArrayList<String>()

        advanceFilter.selectedIds?.let {
            childSfUserIds = if (it.isNotEmpty()) {
                visitProcessHelper.fetchRCBUWiseChildSfUserId(userId, advanceFilter)
            } else {
                visitProcessHelper.fetchChildSfUserIdRoleWise(advanceFilter.sfUserId!!)
            }
        } ?: run {
            childSfUserIds = visitProcessHelper.fetchChildSfUserIdRoleWise(advanceFilter.sfUserId!!)
        }

        val fStatus = if (advanceFilter.status == EnVisitStatus.ALL.value) {
            ArrayList<String>().apply {
                add(EnVisitStatus.PENDING.value)
                add(EnVisitStatus.COMPLETED.value)
            }
        } else {
            ArrayList<String>().apply { add(advanceFilter.status!!) }
        }

        advanceFilter.objectType?.let {
            if (it.isNotEmpty() && it.contains(EnObjectType.ALL.value)) {
                advanceFilter.objectType = EnObjectType.getAllObjectType()
            }
        }

        //TODO: This code is work around. Please remove pageable condition once
        // all the mobile application user moved to latest version
        val visits = if (pageable.pageSize == 20) {
            visitRepository.findAllBySfUserIdsStatusAndObjectType(
                childSfUserIds, fStatus, advanceFilter.objectType,
                advanceFilter.startDatetime, advanceFilter.endDatetime
            )
        } else {
            visitRepository.findAllBySfUserIdsStatusAndObjectType(
                childSfUserIds, fStatus, advanceFilter.objectType,
                advanceFilter.startDatetime, advanceFilter.endDatetime, pageable
            )
        }

        val nVisits = ArrayList<VisitDTO>()

        visits?.forEach {
            nVisits.add(
                VisitDTO().apply {
                    getVisitDTO(it)
                }
            )
        }

        return oneResponse.getSuccessResponse(
            JSONObject().put("visits", JSONArray(objectMapper.writeValueAsString(nVisits)))
                .put(MESSAGE, "All visit fetch successfully")
        )

    }

    @Throws(Exception::class)
    fun getDistanceMatrix(
        userId: Int,
        locationDistanceMatrix: LocationDistanceMatrix
    ): ResponseEntity<String> {

        val dmResponse = hfoNetworkClient.post(
            HFONetworkClient.Endpoints.LOCATION_DISTANCE_MATRIX,
            JSONObject(objectMapper.writeValueAsString(locationDistanceMatrix))
        )

        if (!dmResponse.isSuccess) {

            log("getDistanceMatrix Error - ${dmResponse.message}")
            return oneResponse.getFailureResponse(dmResponse.toJson())

        }

        return oneResponse.getSuccessResponse(
            JSONObject(dmResponse.response)
                .put(
                    MESSAGE,
                    "Distance matrix details fetch successfully"
                )
        )

    }

    fun getDirections(
        userId: Int,
        locationDirections: LocationDirections
    ): ResponseEntity<String> {

        val dmResponse = hfoNetworkClient.post(
            HFONetworkClient.Endpoints.LOCATION_DIRECTIONS,
            JSONObject(objectMapper.writeValueAsString(locationDirections))
        )

        if (!dmResponse.isSuccess) {

            log("getDirections Error - ${dmResponse.message}")
            return oneResponse.getFailureResponse(dmResponse.toJson())

        }

        return oneResponse.getSuccessResponse(
            JSONObject(dmResponse.response)
                .put(
                    MESSAGE,
                    "Directions details fetch successfully"
                )
        )
    }

    fun getAllVisitByObjectDetail(
        objectId: String,
        objectType: String,
    ): ResponseEntity<String> {

        if (!objectId.isNotNullOrNA() || !objectType.isNotNullOrNA())
            return oneResponse.invalidData("Invalid object details")

        val visitList = visitRepository.findAllByObjectIdAndObjectType(objectType, objectId)

        log("getVisitActivityByObjectDetail - visit count: ${visitList?.size}")

        return oneResponse.getSuccessResponse(
            JSONObject().put(
                "visit", visitList ?: run { ArrayList<Visit>() }
            )
        )
    }


    @Throws(Exception::class)
    fun exportVisit(
        userId: Int,
        advanceFilter: AdvanceFilter
    ): ResponseEntity<String> {

        advanceFilter.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                log("exportVisit - ${it.message}")
                return oneResponse.invalidData(it.message)
            }
        }

        val days = DateTimeUtils.getDateDifferenceInDays(
            advanceFilter.startDatetime, advanceFilter.endDatetime
        )

        if (days > 31) {
            val msg = "Visit data cannot be exported for more than 31 days"
            log("exportVisit - $msg")
            return oneResponse.operationFailedResponse(msg)
        }

        val eUser = userRepository.findByUserId(userId)!!

        val visits = visitProcessHelper.prepareVisitData(userId, advanceFilter) ?: run {
            val msg = "No visit data to export!"
            log("exportVisit - $msg")
            return oneResponse.operationFailedResponse(msg)
        }

        visitProcessHelper.visitExportFile(eUser, visits)

        return oneResponse.getSuccessResponse(
            JSONObject()
                .put(
                    MESSAGE, "Visit export process started. " +
                            "You'll receive an email once the process is completed."
                )
        )

    }

    @Throws(Exception::class)
    fun exportVisit(
        advanceFilter: AdvanceFilter
    ): Boolean {

        val visits = visitRepository.findVisitExport(
            advanceFilter.startDatetime,
            advanceFilter.endDatetime
        )

        if (visits!!.isEmpty()) {
            val msg = "No visit data to export!"
            log("exportVisit - $msg")
            return false
        }

        visits.map {
            it.estimatedDuration = it.estimatedDuration?.let { ed -> (ed / 60).roundToOneDecimal() }
            it.estimatedDistance = it.estimatedDistance?.let { ed -> (ed / 1000).roundToOneDecimal() }
            if (!it.objectId.isInvalid()) {
                it.objectSfUrl = if (appProperty.isProduction()) "https://hffc.my.salesforce.com/${it.objectId}"
                else "https://hffc--preprod.sandbox.lightning.force.com/${it.objectId}"
            }
        }

        visitProcessHelper.visitExportFile(
            visits, arrayOf(AJAY_KHETAN_EMAIL),
            arrayOf(RANAN_RODRIGUES_EMAIL, SANJAY_JAISWAR_EMAIL)
        )

        return true

    }

    @Throws(Exception::class)
    fun getRelatedAddress(
        userId: Int,
        mobileNumber: String?,
        objectId: String?,
        objectType: String,
        addressType: String
    ): ResponseEntity<String> {

        if (addressType.isInvalid() ||
            (addressType != EnAddressType.DESTINATION.value && addressType != EnAddressType.ORIGIN.value)
        )
            return oneResponse.invalidData("Invalid address type")

        if (mobileNumber.isInvalid() && objectId.isInvalid()) {
            return oneResponse.invalidData("Invalid data ")
        }

        var addressList: ArrayList<Address>? = ArrayList()

        val address: ArrayList<String>? = if (addressType == EnAddressType.DESTINATION.value) {
            if (objectType == EnObjectType.COLLECTION.value
                || (objectType == EnObjectType.CONNECTOR.value && objectId.isInvalid())
            )
                visitRepository.getDestinationAddressIdByMobile(mobileNumber)
            else {
                visitRepository.getDestinationAddressIdByObjectId(objectId)
            }
        } else {
            if (objectType == EnObjectType.COLLECTION.value
                || (objectType == EnObjectType.CONNECTOR.value && objectId.isInvalid())
            )
                visitRepository.getOriginAddressIdByMobile(mobileNumber)
            else
                visitRepository.getOriginAddressIdByObjectId(objectId)
        }

        if (!address.isNullOrEmpty()) {
            addressList = addressRepository.getAddress(address)
        }

        val addresses = addressList!!.stream().filter { a -> a.isValid(true) }
        return oneResponse.getSuccessResponse(
            JSONObject().put(
                "addressList",
                JSONArray(objectMapper.writeValueAsString(addresses))
            )
        )
    }

    @Throws(Exception::class)
    fun getAllVisitByObject(
        userId: Int,
        objectId: String
    ): ResponseEntity<String> {

        if (objectId.isInvalid()) {
            log("getAllVisitByObject - Invalid objectId")
            return oneResponse.invalidData("Invalid objectId")
        }

        val eUser = userRepository.findByUserId(userId)

        val childSfUserIds = visitProcessHelper.fetchChildSfUserIdRoleWise(eUser!!.sfUserId!!)

        val visits = ArrayList<VisitDTO>()

        visitRepository.findAllByObjectIdAndObjectType(childSfUserIds, objectId)?.forEach {
            visits.add(VisitDTO().apply { getVisitDTO(it) })
        }

        return oneResponse.getSuccessResponse(
            JSONObject().put("visits", JSONArray(objectMapper.writeValueAsString(visits)))
                .put(MESSAGE, "Visits fetched successfully")
        )
    }

}