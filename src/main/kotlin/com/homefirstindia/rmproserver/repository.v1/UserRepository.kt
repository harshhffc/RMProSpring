package com.homefirstindia.rmproserver.repository.v1

import com.homefirstindia.rmproserver.model.v1.UserMapMaster
import com.homefirstindia.rmproserver.model.v1.user.Role
import com.homefirstindia.rmproserver.model.v1.user.User
import com.homefirstindia.rmproserver.model.v1.user.UserLog
import com.homefirstindia.rmproserver.model.v1.user.UserRequest
import com.homefirstindia.rmproserver.model.v1.visit.ItemCount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.*
import kotlin.collections.ArrayList

@Component
class UserRepositoryMaster(
    @Autowired val userRepository: UserRepository
)

@Repository
interface UserLogRepository : JpaRepository<UserLog, String>

@Repository
interface UserRepository : JpaRepository<User, Int> {

    @Query("from User where id = :userId")
    fun findByUserId(userId: Int): User?

    fun findBySfUserId(sfUserId: String): User?

    fun findAllByIsActive(isActive: Boolean = true): ArrayList<User>?

    @Query("FROM User WHERE sfUserId IN :sfUserIds")
    fun findAllBySfUserId(sfUserIds: ArrayList<String>): ArrayList<User>

}
@Repository
interface UserRequestRepository: JpaRepository<UserRequest,String>{

}

@Repository
interface RoleRepository : JpaRepository<Role, Long> {

    fun findByName(name: String?): Role?

}

@Repository
interface UserMapMasterRepository : JpaRepository<UserMapMaster, String> {

    @Query("FROM UserMapMaster where bmId = :userId or clumId = :userId " +
            "or rgmId = :userId or userId = :userId or zmId = :userId")
    fun findAllMappedUsers(userId: String): ArrayList<UserMapMaster>?

    @Query("FROM UserMapMaster where userId IN :userId")
    fun findAllByUserId(userId: ArrayList<String>): ArrayList<UserMapMaster>?

    @Query("SELECT userId FROM UserMapMaster")
    fun getAllUserId(): ArrayList<String>?

    @Query(
        "SELECT new com.homefirstindia.rmproserver.model.v1.visit.ItemCount(u.zoneId, COUNT(u.zoneId)) "
                + "FROM UserMapMaster AS u WHERE u.userId IN :userId " +
                "and (u.zoneId is not null or u.zoneId != '') GROUP BY u.zoneId"
    )
    fun getTotalZones(userId: ArrayList<String>): ArrayList<ItemCount>?

    @Query(
        "SELECT new com.homefirstindia.rmproserver.model.v1.visit.ItemCount(u.regionId, COUNT(u.regionId)) "
                + "FROM UserMapMaster AS u WHERE u.userId IN :userId " +
                "and (u.regionId is not null or u.regionId != '') GROUP BY u.regionId"
    )
    fun getTotalRegions(userId: ArrayList<String>): ArrayList<ItemCount>?

    @Query(
        "SELECT new com.homefirstindia.rmproserver.model.v1.visit.ItemCount(u.clusterId, COUNT(u.clusterId)) "
                + "FROM UserMapMaster AS u WHERE u.userId IN :userId " +
                "and (u.clusterId is not null or u.clusterId != '') GROUP BY u.clusterId"
    )
    fun getTotalClusters(userId: ArrayList<String>): ArrayList<ItemCount>?

    @Query(
        "SELECT new com.homefirstindia.rmproserver.model.v1.visit.ItemCount(u.branchId, COUNT(u.branchId)) "
                + "FROM UserMapMaster AS u WHERE u.userId IN :userId " +
                "and (u.branchId is not null or u.branchId != '') GROUP BY u.branchId"
    )
    fun getTotalBranches(userId: ArrayList<String>): ArrayList<ItemCount>?

}
