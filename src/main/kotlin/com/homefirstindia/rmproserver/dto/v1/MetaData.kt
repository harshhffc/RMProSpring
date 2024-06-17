package com.homefirstindia.rmproserver.dto.v1

import com.homefirstindia.rmproserver.model.v1.*
import com.homefirstindia.rmproserver.model.v1.user.User

class MetaData {

    var id: String? = null
    var name: String? = null
    var sfUserId:String? = null
    var sfBranchId: String? = null
    var sfBmId: String? = null
    var sfClusterId: String? = null
    var sfClusterMangerId: String? = null
    var sfRegionId: String? = null
    var sfRegionalMangerId: String? = null
    var sfZonalManagerId: String? = null
    var zoneId: String? = null

    fun updateUserData(user: User, userMapMaster: UserMapMaster) {

        id = user.id.toString()
        name = user.displayName
        sfUserId = user.sfUserId

        sfBranchId = userMapMaster.branchId
        sfBmId = userMapMaster.bmId
        sfClusterId = userMapMaster.clusterId
        sfClusterMangerId = userMapMaster.clumId
        sfRegionId = userMapMaster.regionId
        sfRegionalMangerId = userMapMaster.rgmId
        sfZonalManagerId = userMapMaster.zmId
        zoneId = userMapMaster.zoneId

    }

    fun updateBranchData(branch: Branch, userMapMaster: UserMapMaster) {

        id = branch.id
        name = branch.name
        sfUserId = branch.sfId

        sfBranchId = userMapMaster.branchId
        sfBmId = userMapMaster.bmId
        sfClusterId = userMapMaster.clusterId
        sfClusterMangerId = userMapMaster.clumId
        sfRegionId = userMapMaster.regionId
        sfRegionalMangerId = userMapMaster.rgmId
        sfZonalManagerId = userMapMaster.zmId
        zoneId = userMapMaster.zoneId

    }

    fun updateClusterData(cluster: Cluster, userMapMaster: UserMapMaster) {

        id = cluster.id
        name = cluster.name
        sfUserId = cluster.sfId

        sfBranchId = userMapMaster.branchId
        sfBmId = userMapMaster.bmId
        sfClusterId = userMapMaster.clusterId
        sfClusterMangerId = userMapMaster.clumId
        sfRegionId = userMapMaster.regionId
        sfRegionalMangerId = userMapMaster.rgmId
        sfZonalManagerId = userMapMaster.zmId
        zoneId = userMapMaster.zoneId

    }

    fun updateRegionData(region: Region, userMapMaster: UserMapMaster) {

        id = region.id
        name = region.name
        sfUserId = region.sfId

        sfBranchId = userMapMaster.branchId
        sfBmId = userMapMaster.bmId
        sfClusterId = userMapMaster.clusterId
        sfClusterMangerId = userMapMaster.clumId
        sfRegionId = userMapMaster.regionId
        sfRegionalMangerId = userMapMaster.rgmId
        sfZonalManagerId = userMapMaster.zmId
        zoneId = userMapMaster.zoneId

    }

    fun updateZoneData(zone: Zone, userMapMaster: UserMapMaster) {

        id = zone.id
        name = zone.name
        sfUserId = zone.sfId

        sfBranchId = userMapMaster.branchId
        sfBmId = userMapMaster.bmId
        sfClusterId = userMapMaster.clusterId
        sfClusterMangerId = userMapMaster.clumId
        sfRegionId = userMapMaster.regionId
        sfRegionalMangerId = userMapMaster.rgmId
        sfZonalManagerId = userMapMaster.zmId
        zoneId = userMapMaster.zoneId

    }

}