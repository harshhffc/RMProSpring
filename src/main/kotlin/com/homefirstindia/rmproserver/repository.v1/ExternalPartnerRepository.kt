package com.homefirstindia.rmproserver.repository.v1

import com.homefirstindia.rmproserver.model.v1.Creds
import com.homefirstindia.rmproserver.model.v1.Partner
import com.homefirstindia.rmproserver.model.v1.PartnerLog
import com.homefirstindia.rmproserver.model.v1.WhitelistedIP
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
class PartnerMasterRepository(
    @Autowired val partnerRepository: PartnerRepository,
    @Autowired val whitelistedIPRepository: WhitelistedIPRepository
)

@Repository
interface PartnerRepository: JpaRepository<Partner, Int> {

    fun findByOrgId(orgId: String): Partner?

}

@Repository
interface PartnerLogRepository: JpaRepository<PartnerLog, String>

@Repository
interface WhitelistedIPRepository : JpaRepository<WhitelistedIP, Int> {

    @Query("from WhitelistedIP where orgId = :orgId and isActive = true")
    fun findAllByOrgId(orgId: String?): ArrayList<WhitelistedIP>?

}

