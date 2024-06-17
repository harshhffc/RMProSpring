package com.homefirstindia.rmproserver.services.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.rmproserver.dto.v1.MetaData
import com.homefirstindia.rmproserver.dto.v1.RemarkDTO
import com.homefirstindia.rmproserver.helper.v1.UserLogHelper
import com.homefirstindia.rmproserver.helper.v1.UserProcessHelper
import com.homefirstindia.rmproserver.model.v1.UserMapMaster
import com.homefirstindia.rmproserver.model.v1.user.User
import com.homefirstindia.rmproserver.networking.v1.CMSNetworkingClient
import com.homefirstindia.rmproserver.repository.v1.*
import com.homefirstindia.rmproserver.utils.*
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.net.URLEncoder

@Service
class UserService(
    @Autowired val oneResponse: OneResponse,
    @Autowired val userRepository: UserRepository,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val userMapMasterRepository: UserMapMasterRepository,
    @Autowired val branchRepository: BranchRepository,
    @Autowired val clusterRepository: ClusterRepository,
    @Autowired val zoneRepository: ZoneRepository,
    @Autowired val regionRepository: RegionRepository,
    @Autowired val appUpdateRepository: AppUpdateRepository,
    @Autowired val cmsNetworkClient: CMSNetworkingClient,
    @Autowired val userProcessHelper: UserProcessHelper
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    @Throws(Exception::class)
    fun getMetaData(userId: Int): ResponseEntity<String> {

        val eUser = userRepository.findByUserId(userId)!!

        eUser.roles?.let {
            if (it.isEmpty())
                return oneResponse.getFailureResponse(JSONObject().put(MESSAGE, "No role found!"))
        }


        val role = eUser.roles?.first().let {
            it?.name
        }

        val mappedUsers: ArrayList<UserMapMaster>? = if (role != EnRole.HO_USER.value) {
            userMapMasterRepository.findAllMappedUsers(eUser.sfUserId!!)
        } else {
            userMapMasterRepository.findAll() as ArrayList<UserMapMaster>
        }

        mappedUsers?.let { mapMasters ->
            mapMasters.filter {
                it.userId != eUser.sfUserId
            } as ArrayList<UserMapMaster>
        }

        mappedUsers?.let {
            if (it.isEmpty()) {
                return oneResponse.getSuccessResponse(
                    getMetaDataJson(eUser, it)
                )
            }
        } ?: run {
            return oneResponse.getSuccessResponse(
                getMetaDataJson(eUser)
            )
        }

        val zones = ArrayList<MetaData>()

        val mapMasterGroupByZone = mappedUsers.groupBy { it.zoneId }

        mapMasterGroupByZone.forEach { (key, value) ->
            key?.let {
                if (!it.isInvalid()) {
                    val eZone = zoneRepository.findById(it)
                    eZone.get().let { zone ->
                        if (value.isNotEmpty()) {
                            val singleMapMaster = value.first()

                            val metaData = MetaData().apply {
                                this.updateZoneData(zone, singleMapMaster)
                            }
                            zones.add(metaData)
                        }
                    }
                }
            }
        }

        val regions = ArrayList<MetaData>()

        val mapMasterGroupByRegion = mappedUsers.groupBy { it.regionId }

        mapMasterGroupByRegion.forEach { (key, value) ->
            key?.let {
                if (!it.isInvalid()) {
                    val eRegion = regionRepository.findBySfId(it)
                    eRegion?.let { region ->
                        if (value.isNotEmpty()) {
                            val singleMapMaster = value.first()

                            val metaData = MetaData().apply {
                                this.updateRegionData(region, singleMapMaster)
                            }
                            regions.add(metaData)
                        }
                    }
                }
            }
        }

        val clusters = ArrayList<MetaData>()

        val mapMasterGroupByCluster = mappedUsers.groupBy { it.clusterId }

        mapMasterGroupByCluster.forEach { (key, value) ->
            key?.let {
                if (!it.isInvalid()) {
                    val eCluster = clusterRepository.findBySfId(it)
                    eCluster?.let { cluster ->
                        if (value.isNotEmpty()) {
                            val singleMapMaster = value.first()

                            val metaData = MetaData().apply {
                                this.updateClusterData(cluster, singleMapMaster)
                            }
                            clusters.add(metaData)
                        }
                    }
                }
            }
        }

        val branches = ArrayList<MetaData>()

        val mapMasterGroupByBranch = mappedUsers.groupBy { it.branchId }

        mapMasterGroupByBranch.forEach { (key, value) ->
            key?.let {
                val eBranch = branchRepository.findBySfId(it)
                eBranch?.let { branch ->
                    if (value.isNotEmpty()) {
                        val singleMapMaster = value.first()
                        val metaData = MetaData().apply {
                            this.updateBranchData(branch, singleMapMaster)
                        }
                        branches.add(metaData)
                    }
                }
            }
        }

        val users = ArrayList<MetaData>()

        userRepository.findAllBySfUserId(
            mappedUsers
                .filter { it.userId.isNotNullOrNA() }
                .map { it.userId.toString() }
                .toCollection(ArrayList())
        ).forEach { user ->
            val mappedUserData = mappedUsers.find { u -> u.userId.equals(user.sfUserId) }
            mappedUserData?.let {
                val metaData = MetaData().apply {
                    this.updateUserData(user, it)
                }
                users.add(metaData)
            }
        }

        return oneResponse.getSuccessResponse(
            getMetaDataJson(eUser, mappedUsers, users, branches, clusters, regions, zones)
        )

    }

    @Throws(Exception::class)
    fun getMetaDataJson(
        eUser: User,
        userMapMasters: ArrayList<UserMapMaster>? = null,
        users: ArrayList<MetaData>? = null,
        branches: ArrayList<MetaData>? = null,
        clusters: ArrayList<MetaData>? = null,
        regions: ArrayList<MetaData>? = null,
        zones: ArrayList<MetaData>? = null
    ): JSONObject {

        return JSONObject().apply {
            this.put(USER, JSONObject(objectMapper.writeValueAsString(eUser)))
            this.put("userMapMasters", userMapMasters ?: ArrayList<MetaData>())
            this.put("users", users ?: ArrayList<MetaData>())
            this.put("branches", branches ?: ArrayList<MetaData>())
            this.put("clusters", clusters ?: ArrayList<MetaData>())
            this.put("regions", regions ?: ArrayList<MetaData>())
            this.put("zones", zones ?: ArrayList<MetaData>())

        }

    }

    @Throws(Exception::class)
    fun getAppUpdateInfo(): ResponseEntity<String> {

        val activeAppUpdateInfo = appUpdateRepository.findByIsActiveTrue()

        activeAppUpdateInfo?.let {

            if (it.isEmpty() || it.size > 1) {
                log("getAppUpdateInfo - More than one app update info found")
                return oneResponse.operationFailedResponse("No app update info found")
            }

        } ?: run {
            return oneResponse.operationFailedResponse("No app update info found")
        }

        return oneResponse.getSuccessResponse(
            JSONObject(objectMapper.writeValueAsString(activeAppUpdateInfo[0]))
        )
    }

    @Throws(Exception::class)
    fun addRemark(
        userId: Int,
        remarkDTO: RemarkDTO
    ): ResponseEntity<String>? {

        remarkDTO.mandatoryFieldsCheck().let {
            if (!it.isSuccess) {
                val msg = it.message
                log("addRemark - $msg")
                return oneResponse.invalidData(msg)
            }
        }

        remarkDTO.remark = URLEncoder.encode(remarkDTO.remark, "UTF-8")

        val lResponse = userProcessHelper.addRemark(remarkDTO)

        if (!lResponse.isSuccess) {
            log("addRemark - Fail to add remark for objectId: ${remarkDTO.objectId} | CMS response: ${lResponse.message}")
            return oneResponse.operationFailedResponse("Failed to add remark")
        }

        return oneResponse.getSuccessResponse(
            JSONObject().put(MESSAGE, "Remark added successfully.")
        )

    }

    @Throws(Exception::class)
    fun getAllRemark(
        userId: Int,
        remarkDTO: RemarkDTO
    ): ResponseEntity<String>? {

        remarkDTO.mandatoryFieldsCheckForGetRemarks().let {
            if (!it.isSuccess) {
                val msg = it.message
                log("getAllRemark - $msg")
                return oneResponse.invalidData(msg)
            }
        }

        val dmResponse = cmsNetworkClient.post(
            CMSNetworkingClient.Endpoints.GET_REMARK.value,
            JSONObject(objectMapper.writeValueAsString(remarkDTO))
        )

        if (!dmResponse.isSuccess) {
            log("getAllRemark - CMS response: ${dmResponse.stringEntity}")
            return oneResponse.operationFailedResponse("No remarks found")
        }

        val rJson = JSONObject(dmResponse.stringEntity)

        val remarkJsonArray = rJson.optJSONObject("remarks").optJSONArray("content")

        return oneResponse.getSuccessResponse(
            JSONObject().apply {
                put(REMARKS, JSONArray(remarkJsonArray))
                put(MESSAGE, "Remark fetched successfully.")
            }
        )

    }

}