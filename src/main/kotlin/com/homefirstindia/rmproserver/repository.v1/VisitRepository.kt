package com.homefirstindia.rmproserver.repository.v1

import com.homefirstindia.rmproserver.dto.v1.VisitDashboardDTO
import com.homefirstindia.rmproserver.dto.v1.VisitExport
import com.homefirstindia.rmproserver.dto.v1.VisitResponseDTO
import com.homefirstindia.rmproserver.model.v1.common.Address
import com.homefirstindia.rmproserver.model.v1.visit.Visit
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface VisitRepository : JpaRepository<Visit, Int> {

    fun findById(id: String): Visit?

    @Query(
        "select new com.homefirstindia.rmproserver.dto.v1.VisitDashboardDTO(v.status, count(v.status), v.objectType)" +
                " from Visit v where v.sfUserId IN :sfUserIds AND v.createDatetime between :startDatetime AND :endDatetime" +
                " group by v.status, v.objectType"
    )
    fun findVisitStatsBetweenDateBySfUserIds(
        sfUserIds: ArrayList<String>,
        startDatetime: String?,
        endDatetime: String?
    ): ArrayList<VisitDashboardDTO>?

    @Query(
        "FROM Visit where sfUserId IN :sfUserIds " +
                "AND (startDatetime >= :startDatetime AND startDatetime <= :endDatetime) " +
                "order by createDatetime desc"
    )
    fun findAllBetweenDateBySfUserIds(
        sfUserIds: ArrayList<String>,
        startDatetime: String?,
        endDatetime: String?
    ): ArrayList<Visit>?

    @Query(
        "FROM Visit WHERE sfUserId IN :sfUserIds AND status IN :status AND objectType IN :objectType " +
                "AND (createDatetime >= :startDatetime AND createDatetime <= :endDatetime) " +
                "order by createDatetime desc"
    )
    fun findAllBySfUserIdsStatusAndObjectType(
        sfUserIds: ArrayList<String>?, status: ArrayList<String>?,
        objectType: ArrayList<String>?, startDatetime: String?,
        endDatetime: String?, pageable: Pageable
    ): ArrayList<Visit>?

    @Query(
        "FROM Visit WHERE sfUserId IN :sfUserIds AND status IN :status AND objectType IN :objectType " +
                "AND (createDatetime >= :startDatetime AND createDatetime <= :endDatetime) " +
                "order by createDatetime desc"
    )
    fun findAllBySfUserIdsStatusAndObjectType(
        sfUserIds: ArrayList<String>?, status: ArrayList<String>?,
        objectType: ArrayList<String>?, startDatetime: String?,
        endDatetime: String?
    ): ArrayList<Visit>?

    @Query(
        "select new com.homefirstindia.rmproserver.dto.v1.VisitResponseDTO(v.id, v.status, v.reason, " +
                "v.createDatetime) FROM Visit v WHERE v.objectType = :objectType and v.objectId = :objectId " +
                "order by v.createDatetime"
    )
    fun findAllByObjectIdAndObjectType(
        objectType: String,
        objectId: String
    ): ArrayList<VisitResponseDTO>?

    @Query(
        "SELECT new com.homefirstindia.rmproserver.dto.v1.VisitExport(v.id," +
                " v.username, v.firstName, v.objectId, v.objectType, v.travelMode," +
                " v.remark, v.reason, v.result, v.status, v.estimatedDistance," +
                " v.estimatedDuration, v.startDatetime," +
                " v.endDatetime, v.sfUserId, v.ptpDate, b.name, c.name, r.name)" +
                " FROM Visit v LEFT JOIN UserMapMaster umm ON umm.userId = v.sfUserId" +
                " LEFT JOIN Branch b ON umm.branchId = b.sfId" +
                " LEFT JOIN Cluster c ON umm.clusterId = c.sfId" +
                " LEFT JOIN Region r ON umm.regionId = r.sfId" +
                " WHERE v.createDatetime >= :startDatetime AND v.createDatetime <= :endDatetime" +
                " AND v.sfUserId IN :sfUserIds" +
                " ORDER BY v.createDatetime DESC"
    )
    fun findVisitExport(
        sfUserIds: ArrayList<String>, startDatetime: String?,
        endDatetime: String?
    ): ArrayList<VisitExport>?

    @Query(
        "SELECT new com.homefirstindia.rmproserver.dto.v1.VisitExport(v.id," +
                " v.username, v.firstName, v.objectId, v.objectType, v.travelMode," +
                " v.remark, v.reason, v.result, v.status, v.estimatedDistance," +
                " v.estimatedDuration, v.startDatetime," +
                " v.endDatetime, v.sfUserId, v.ptpDate, b.name, c.name, r.name)" +
                " FROM Visit v LEFT JOIN UserMapMaster umm ON umm.userId = v.sfUserId" +
                " LEFT JOIN Branch b ON umm.branchId = b.sfId" +
                " LEFT JOIN Cluster c ON umm.clusterId = c.sfId" +
                " LEFT JOIN Region r ON umm.regionId = r.sfId" +
                " WHERE v.createDatetime >= :startDatetime AND v.createDatetime <= :endDatetime" +
                " ORDER BY v.createDatetime DESC"
    )
    fun findVisitExport(
        startDatetime: String?,
        endDatetime: String?
    ): ArrayList<VisitExport>?


    @Query(
        "SELECT v.destinationAddressId FROM " +
                "Visit v where v.objectId = :objectId", nativeQuery = true
    )
    fun getDestinationAddressIdByObjectId(
        objectId: String?
    ): ArrayList<String>?

    @Query(
        "SELECT v.originAddressId FROM " +
                "Visit v where v.objectId = :objectId", nativeQuery = true
    )
    fun getOriginAddressIdByObjectId(
        objectId: String?
    ): ArrayList<String>?


    @Query(
        "SELECT v.destinationAddressId FROM " +
                "Visit v where v.mobileNumber = :mobileNumber", nativeQuery = true
    )
    fun getDestinationAddressIdByMobile(
        mobileNumber: String?
    ): ArrayList<String>?

    @Query(
        "SELECT v.originAddressId FROM " +
                "Visit v where v.mobileNumber = :mobileNumber", nativeQuery = true
    )
    fun getOriginAddressIdByMobile(
        mobileNumber: String?
    ): ArrayList<String>?

    @Query("FROM Visit WHERE sfUserId IN :sfUserIds AND objectId = :objectId order by createDatetime desc")
    fun findAllByObjectIdAndObjectType(sfUserIds: ArrayList<String>?, objectId: String): ArrayList<Visit>?

}