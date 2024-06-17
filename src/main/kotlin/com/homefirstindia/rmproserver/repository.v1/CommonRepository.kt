package com.homefirstindia.rmproserver.repository.v1

import com.homefirstindia.rmproserver.model.v1.Creds
import com.homefirstindia.rmproserver.model.v1.common.AppUpdateInfo
import com.homefirstindia.rmproserver.model.v1.common.Attachment
import com.homefirstindia.rmproserver.model.v1.common.Reason
import com.homefirstindia.rmproserver.model.v1.common.Result
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CredsRepository : JpaRepository<Creds, String> {

    @Query("from Creds where partnerName = :partnerName and credType = :credType and isValid = true")
    fun findByPartnerNameAndCredType(partnerName: String, credType: String): Creds?

}

@Repository
interface ReasonRepository : JpaRepository<Reason, String> {
    fun findByNameAndType(name: String?, type: String?): Reason?
}

@Repository
interface ResultRepository : JpaRepository<Result, String> {
    fun findByNameAndType(name: String?, type: String?): Result?
}
@Repository
interface AttachmentRepository : JpaRepository<Attachment,String>

@Repository
interface AppUpdateRepository : JpaRepository<AppUpdateInfo, String> {

    fun findByIsActiveTrue() : ArrayList<AppUpdateInfo>?

}