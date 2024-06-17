package com.homefirstindia.rmproserver.helper.v1

import com.homefirstindia.rmproserver.model.v1.*
import com.homefirstindia.rmproserver.model.v1.user.Role
import com.homefirstindia.rmproserver.model.v1.user.User
import com.homefirstindia.rmproserver.networking.v1.SFConnection
import com.homefirstindia.rmproserver.repository.v1.*
import com.homefirstindia.rmproserver.utils.*
import com.homefirstindia.rmproserver.utils.LoggerUtils.log
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class SFSyncHelper(
    @Autowired val sfSyncRepositoryMaster: SFSyncRepositoryMaster,
    @Autowired val userRepository: UserRepository,
    @Autowired val userMapMasterRepository: UserMapMasterRepository,
    @Autowired val roleRepository: RoleRepository,
    @Autowired val branchRepository: BranchRepository,
    @Autowired val clusterRepository: ClusterRepository,
    @Autowired val regionRepository: RegionRepository,
    @Autowired val zoneRepository: ZoneRepository,
    @Autowired val sfConnection: SFConnection

) {
    fun syncBranch(json: JSONObject?): ArrayList<Branch> {

        val branches = ArrayList<Branch>()

        val allBranchNameKey = sfSyncRepositoryMaster.branchNameKeyRepository.findAll()

        if (null != json) {

            if (json.getInt("totalSize") > 0) {

                val recordArray = json.getJSONArray("records")

                for (i in 0 until recordArray.length()) {

                    val singleBranchJson = recordArray.getJSONObject(i)
                    val singleBranch = Branch(singleBranchJson)

                    allBranchNameKey.find {
                        it.branchName == singleBranch.name
                    }?.let {
                        singleBranch.branchKey = it.branchKey
                    }

                    val existingBranch: Branch? = sfSyncRepositoryMaster.branchRepository.findBySfId(singleBranch.sfId!!)

                    if (null != existingBranch) {
                        singleBranch.id = existingBranch.id
                        singleBranch.address!!.id = existingBranch.address?.id
                        singleBranch.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                    }

                    branches.add(singleBranch)

                }
            }
        }
        return branches

    }

    fun syncCluster(json: JSONObject?): ArrayList<Cluster> {

        val clusters = ArrayList<Cluster>()

        if (null != json) {

            if (json.getInt("totalSize") > 0) {

                val recordArray = json.getJSONArray("records")

                for (i in 0 until recordArray.length()) {

                    val singleClusterJson = recordArray.getJSONObject(i)
                    val singleCluster = Cluster(singleClusterJson)

                    val existing: Cluster? =
                        sfSyncRepositoryMaster.clusterRepository.findBySfId(singleCluster.sfId!!)

                    if (null != existing) {
                        singleCluster.id = existing.id
                        singleCluster.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                    }

                    clusters.add(singleCluster)

                }

            }

        }

        return clusters

    }

    fun syncRegion(json: JSONObject?): ArrayList<Region> {

        val regions = ArrayList<Region>()

        if (null != json) {

            if (json.getInt("totalSize") > 0) {

                val recordArray = json.getJSONArray("records")

                for (i in 0 until recordArray.length()) {

                    val singleRegionJson = recordArray.getJSONObject(i)
                    val singleRegion = Region(singleRegionJson)

                    val existing: Region? =
                        sfSyncRepositoryMaster.regionRepository.findBySfId(singleRegion.sfId!!)

                    if (null != existing) {

                        if (!existing.zoneId.isInvalid())
                            singleRegion.zoneId = existing.zoneId

                        if (!existing.zonalManager.isInvalid())
                            singleRegion.zonalManager = existing.zonalManager

                        if (!existing.zonalManagerSfId.isInvalid())
                            singleRegion.zonalManagerSfId = existing.zonalManagerSfId

                        singleRegion.id = existing.id
                        singleRegion.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

                    }

                    regions.add(singleRegion)
                }

            }

        }

        return regions

    }

    fun addZone() {
        createZone("North & Central", "Abhijeet Jamkhindikar", "0059000000TnzidAAB")
        createZone("South", "Arunchandra Jupalli", "0059000000TohopAAB")
        createZone("South GJ & West", "Deepak Venkeshwar", "0052j000000m8cLAAQ")
    }

    private fun createZone(zoneName: String, managerName: String, sfId: String): Zone? {
        return try {


            val eZone = zoneRepository.findBySfId(sfId)

            eZone ?: run {
                zoneRepository.save(Zone().apply {
                    name = zoneName
                    zonalManager = managerName
                    this.sfId = sfId
                    updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                })
            }

        } catch (e: Exception) {
            log("createZone - Error : ${e.message}")
            null
        }
    }

    fun syncUser(userSfJson: JSONObject?): ArrayList<User> {

        val users = ArrayList<User>()

        var userSfJsonCopy = userSfJson

        if (null != userSfJsonCopy) {

            val totalRecordSize = userSfJsonCopy.getInt("totalSize")

            if (totalRecordSize > 0) {

                users.addAll(parseSfUserJson(userSfJsonCopy))

                if (totalRecordSize > 2000) {

                    while (!userSfJsonCopy!!.optBoolean("done", true)) {

                        userSfJsonCopy = sfConnection.getNextRecords(userSfJsonCopy.getString("nextRecordsUrl"))

                        userSfJsonCopy?.let {
                            users.addAll(parseSfUserJson(it))
                        }

                    }

                }

            }

        }

        val eUsers = userRepository.findAll()

        users.forEach{nUser ->
            eUsers.forEach { eUser ->
                eUser.sfUserId?.let {uSfId ->
                    nUser.sfUserId?.let { nSfId ->
                        if (uSfId.contains(nSfId)) {
                            nUser.updateUser(eUser)
                        }
                    }
                }
            }
        }

        return users

    }

    fun parseSfUserJson(json: JSONObject): ArrayList<User> {

        val users = ArrayList<User>()

        val recordArray = json.getJSONArray("records")

        for (i in 0 until recordArray.length()) {

            val singleUserJson = recordArray.getJSONObject(i)
            users.add(User(singleUserJson))

        }

        return users

    }

    fun processRoleLink() {

        val eUser = userRepository.findAll()
        val roles = roleRepository.findAll()

        eUser.forEach {

            if (it.sfUserRoleId.isNotNullOrNA()) {

                val userRoleName = it.sfUserRoleName ?: return@forEach

                val role = with(userRoleName) {

                    when {

                        startsWith(ROLE_RGM) || startsWith(KEY_ROLE_RGM) -> {
                            roles.find { r -> r.name == ROLE_RGM }
                        }

                        startsWith(ROLE_CSM) -> {
                            roles.find { r -> r.name == ROLE_CSM }
                        }

                        startsWith(ROLE_RM) || startsWith(KEY_ROLE_RM) -> {
                            roles.find { r -> r.name == ROLE_RM }
                        }

                        startsWith(ROLE_CM) || startsWith(KEY_ROLE_CLUM) -> {
                            roles.find { r -> r.name == ROLE_CM }
                        }

                        startsWith(ROLE_BM) || startsWith(KEY_ROLE_BM)
                                || startsWith(KEY_ROLE_BRANCH) -> {
                            roles.find { r -> r.name == ROLE_BM }
                        }

                        startsWith(ROLE_ZM) -> {
                            roles.find { r -> r.name == ROLE_ZM }
                        }

                        else -> {
                            roles.find { r -> r.name == ROLE_HO_USER }
                        }
                    }
                }

                if (null != role) {

                    val nRoles = ArrayList<Role>()
                    nRoles.add(role)
                    it.roles = nRoles

                }

            }

        }

        userRepository.saveAll(eUser)

    }

    fun addAllRole() {
        createRole(EnRole.SUPER_ADMIN.value)
        createRole(EnRole.ADMIN.value)
        createRole(EnRole.MANAGEMENT.value)
        createRole(EnRole.HO_USER.value)
        createRole(EnRole.RGM.value)
        createRole(EnRole.ZM.value)
        createRole(EnRole.CM.value)
        createRole(EnRole.BM.value)
        createRole(EnRole.RM.value)
        createRole(EnRole.CSM.value)
    }

    private fun createRole(name: String): Role? {

        return try {

            val eRole = roleRepository.findByName(name)

            eRole ?: run {
                println("Creating new role: $name")
                roleRepository.save(Role().apply {
                    this.name = name
                    updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                })
            }

        } catch (e: Exception) {
            log("createRole - Error : ${e.message}")
            null
        }

    }

    @Transactional
    fun processUserMapMaster() {

        val allBranches = branchRepository.findAll()
        val allCluster = clusterRepository.findAll()
        val allRegions = regionRepository.findAll()
        val allZone = zoneRepository.findAll()
        val allActiveUsers = userRepository.findAllByIsActive()

        val allUserMaps = ArrayList<UserMapMaster>()

        // Code to fill Branch users in userMapMaster Table
        allActiveUsers!!.forEach { user ->

            val userMaster = UserMapMaster()

            userMaster.userId = user.sfUserId

            val userKey = user.sfUserRoleName!!
            // Search user in branch

            // Branch Found, now create the userMap with the data
            allBranches.find {
                userKey.contains("-")
                        && it.branchKey != null
                        && userKey.split("-", limit = 2)[1].trim() == it.branchKey!!
            }?.let {
                userMaster.branchId = it.sfId
                userMaster.bmId = it.branchManagerSfId
                userMaster.clusterId = it.sfClusterId
                userMaster.clumId = it.clusterManagerSfId
                userMaster.regionId = it.sfRegionId
                userMaster.rgmId = it.regionalManagerSfId
            }

            // Cluster Found, now create the userMap with the data
            allCluster.find {
                userKey.contains("-")
                        && it.name != null
                        && userKey.split("-", limit = 2)[1].trim() == (it.name!!)
            }?.let {
                userMaster.clusterId = it.sfId
                userMaster.clumId = it.clusterManagerSfId
                userMaster.regionId = it.sfRegionId
                userMaster.rgmId = it.regionalManagerSfId
            }

            allRegions.find {
                userKey.contains("-")
                        && it.name != null
                        && userKey.split("-", limit = 2)[1].trim() == (it.name!!)
            }?.let {
                userMaster.regionId = it.sfId
                userMaster.rgmId = it.regionalManagerSfId
                userMaster.zoneId = it.zoneId
                userMaster.zmId = it.zonalManagerSfId
            }

            allZone.find {
                userKey.contains("-")
                        && it.name != null
                        && userKey.split("-", limit = 2)[1].trim() == (it.name!!)
            }?.let {
                userMaster.zmId = it.sfId
                userMaster.zoneId = it.id
            }

            userMaster.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
            allUserMaps.add(userMaster)

        }

        userMapMasterRepository.deleteAll()
        userMapMasterRepository.saveAll(allUserMaps)

    }

    @Transactional
    fun processZoneMapToMapMaster() {

        val allUserMaps = userMapMasterRepository.findAll()
        val allRegions = regionRepository.findAll()

        allUserMaps.forEach {uMapMaster ->

            allRegions.forEach {
                if (!it.sfId.isInvalid()
                    && it.sfId == uMapMaster.regionId) {
                    uMapMaster.zoneId = it.zoneId
                    uMapMaster.zmId = it.zonalManagerSfId

                    userMapMasterRepository.save(uMapMaster)
                }
            }

        }

    }
}
