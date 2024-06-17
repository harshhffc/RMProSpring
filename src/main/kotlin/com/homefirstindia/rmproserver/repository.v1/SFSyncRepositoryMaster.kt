package com.homefirstindia.rmproserver.repository.v1

import com.homefirstindia.rmproserver.model.v1.*
import com.homefirstindia.rmproserver.model.v1.user.BranchNameKeyMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class SFSyncRepositoryMaster(
    @Autowired val regionRepository: RegionRepository,
    @Autowired val branchRepository: BranchRepository,
    @Autowired val branchNameKeyRepository: BranchNameKeyRepository,
    @Autowired val clusterRepository: ClusterRepository,
    @Autowired val userRepository: UserRepository
)

@Repository
interface BranchRepository : JpaRepository<Branch, String> {
    fun findBySfId(sfId: String): Branch?
}

@Repository
interface BranchNameKeyRepository : JpaRepository<BranchNameKeyMap, String>

@Repository
interface ClusterRepository : JpaRepository<Cluster, String> {
    fun findBySfId(sfId: String): Cluster?
}

@Repository
interface RegionRepository : JpaRepository<Region, String> {
    fun findBySfId(sfId: String): Region?

}

@Repository
interface ZoneRepository : JpaRepository<Zone, String> {
    fun findBySfId(sfId: String): Zone?
}
