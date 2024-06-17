package com.homefirstindia.rmproserver.helper.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.rmproserver.dto.v1.AdvanceFilter
import com.homefirstindia.rmproserver.dto.v1.VisitExport
import com.homefirstindia.rmproserver.manager.v1.AmazonClient
import com.homefirstindia.rmproserver.manager.v1.EnS3BucketPath
import com.homefirstindia.rmproserver.model.v1.UserMapMaster
import com.homefirstindia.rmproserver.model.v1.common.Attachment
import com.homefirstindia.rmproserver.model.v1.common.MFile
import com.homefirstindia.rmproserver.model.v1.user.User
import com.homefirstindia.rmproserver.model.v1.user.UserRequest
import com.homefirstindia.rmproserver.repository.v1.*
import com.homefirstindia.rmproserver.security.AppProperty
import com.homefirstindia.rmproserver.utils.*
import com.opencsv.CSVWriter
import com.opencsv.bean.StatefulBeanToCsvBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileWriter

@Service
class VisitProcessHelper(
    @Autowired val oneResponse: OneResponse,
    @Autowired val appProperty: AppProperty,
    @Autowired val visitRepository: VisitRepository,
    @Autowired val amazonClient: AmazonClient,
    @Autowired val userRequestRepository: UserRequestRepository,
    @Autowired val userRepository: UserRepository,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired val attachmentRepository: AttachmentRepository,
    @Autowired val mailHelper: MailHelper,
    @Autowired val userMapMasterRepository: UserMapMasterRepository,

    ){
    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    fun prepareVisitData(userId: Int, advanceFilter: AdvanceFilter): ArrayList<VisitExport>? {

        var childSfUserIds = ArrayList<String>()

        advanceFilter.selectedIds?.let {
            childSfUserIds = if (it.isNotEmpty())
                fetchRCBUWiseChildSfUserId(userId, advanceFilter)
            else
                fetchChildSfUserIdRoleWise(advanceFilter.sfUserId!!)
        } ?: run {
            childSfUserIds = fetchChildSfUserIdRoleWise(advanceFilter.sfUserId!!)
        }

        val visits = visitRepository.findVisitExport(
            childSfUserIds, advanceFilter.startDatetime,
            advanceFilter.endDatetime
        )

        if (visits!!.isEmpty()) {
            val msg = "No visit data to export!"
            log("exportVisit - $msg")
            return null
        }

        visits.map {
            it.estimatedDuration = it.estimatedDuration?.let { ed -> (ed / 60).roundToOneDecimal() }
            it.estimatedDistance = it.estimatedDistance?.let { ed -> (ed / 1000).roundToOneDecimal() }
            if (!it.objectId.isInvalid()) {
                it.objectSfUrl = if (appProperty.isProduction()) "https://hffc.my.salesforce.com/${it.objectId}"
                else "https://hffc--preprod.sandbox.lightning.force.com/${it.objectId}"
            }
        }

        return visits

    }

    fun fetchRCBUWiseChildSfUserId(userId: Int, advanceFilter: AdvanceFilter): ArrayList<String> {

        val sfUserIds = ArrayList<String>()

        val eUser = userRepository.findByUserId(userId)

        eUser?.let { user ->

            val role = user.roles?.first().let {
                it?.name
            }

            val mappedUsers: ArrayList<UserMapMaster>? = if (role != EnRole.HO_USER.value) {
                userMapMasterRepository.findAllMappedUsers(user.sfUserId!!)
            } else {
                userMapMasterRepository.findAll() as ArrayList<UserMapMaster>
            }

            mappedUsers?.forEach {

                advanceFilter.selectedIds?.let { ids ->

                    if (advanceFilter.idsType == ZONE) {
                        if (ids.contains(it.zoneId)) {
                            it.userId?.let { uId ->
                                sfUserIds.add(uId)
                            }
                        }
                    } else if (advanceFilter.idsType == REGION) {
                        if (ids.contains(it.regionId)) {
                            it.userId?.let { uId ->
                                sfUserIds.add(uId)
                            }
                        }
                    } else if (advanceFilter.idsType == CLUSTER) {
                        if (ids.contains(it.clusterId)) {
                            it.userId?.let { uId ->
                                sfUserIds.add(uId)
                            }
                        }
                    } else if (advanceFilter.idsType == BRANCH) {
                        if (ids.contains(it.branchId)) {
                            it.userId?.let { uId ->
                                sfUserIds.add(uId)
                            }
                        }
                    } else {
                        if (ids.contains(it.userId)) {
                            it.userId?.let { uId ->
                                sfUserIds.add(uId)
                            }
                        }
                    }

                }

            }

        }

        return sfUserIds
    }

    fun fetchChildSfUserIdRoleWise(sfUserId: String): ArrayList<String> {

        val sfUserIds = ArrayList<String>()

        userRepository.findBySfUserId(sfUserId)?.let { eUser ->

            val role = eUser.roles?.first().let {
                it?.name
            }

            sfUserIds.add(eUser.sfUserId!!)

            if (role == EnRole.HO_USER.value) {
                userMapMasterRepository.getAllUserId()?.let {
                    return it
                }
            }

            userMapMasterRepository.findAllMappedUsers(eUser.sfUserId!!)?.forEach {

                when (role) {
                    EnRole.ZM.value -> {
                        it.zmId?.let { rmId ->
                            if (rmId == eUser.sfUserId) {
                                it.userId?.let { uId ->
                                    sfUserIds.add(uId)
                                }
                            }
                        }
                    }

                    EnRole.RGM.value -> {
                        it.rgmId?.let { rmId ->
                            if (rmId == eUser.sfUserId) {
                                it.userId?.let { uId ->
                                    sfUserIds.add(uId)
                                }
                            }
                        }
                    }

                    EnRole.CM.value -> {
                        it.clumId?.let { cmId ->
                            if (cmId == eUser.sfUserId) {
                                it.userId?.let { uId ->
                                    sfUserIds.add(uId)
                                }
                            }
                        }
                    }

                    EnRole.BM.value -> {
                        it.bmId?.let { bmId ->
                            if (bmId == eUser.sfUserId) {
                                it.userId?.let { uId ->
                                    sfUserIds.add(uId)
                                }
                            }
                        }
                    }
                }

            }

        }

        return sfUserIds

    }

    @Async(THREAD_POOL_TASK_EXECUTOR)
    fun visitExportFile(eUser: User, visitExports: ArrayList<VisitExport>?) {

        val userRequest = UserRequest().apply {
            requestStatus = EnUserRequestStatus.CREATED.value
            userId = eUser.id.toString()
            email = eUser.email
            userName = eUser.displayName
            requestType = AttachmentType.RM_PRO_VISIT_EXPORT.value
            rawRequest = objectMapper.writeValueAsString(visitExports)
        }

        userRequestRepository.save(userRequest)

        val fileName = "VisitData_${csvDateFormat()}.csv"
        val visitCsvFilePath = "${appProperty.filePath}$fileName"
        val writer = FileWriter(visitCsvFilePath)
        val sbc = StatefulBeanToCsvBuilder<VisitExport>(writer)
            .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
            .build()

        sbc.write(visitExports)
        writer.flush()
        writer.close()

        val file = File(visitCsvFilePath)

        if (!amazonClient.uploadFile(fileName, file, EnS3BucketPath.VISIT_EXPORT)) {

            log("visitExportFile - Failed to upload visit export file on S3")

            userRequest.requestStatus = EnUserRequestStatus.FAILED.value
            userRequest.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
            userRequest.description = "Failed to upload visit export file on S3"
            userRequestRepository.save(userRequest)

        }

        val attachment = Attachment().apply {
            this.fileName = fileName
            fileIdentifier = cryptoUtils.getFileIdentifier()
            objectId = userRequest.id
            objectType = MyObject.USER_REQUEST.value
            contentType = FileTypesExtentions.CSV.displayName
            attachmentType = AttachmentType.RM_PRO_VISIT_EXPORT.value
        }

        attachmentRepository.save(attachment)

        userRequest.isProcessed = true

        userRequest.requestStatus = EnUserRequestStatus.PARTIALLY_COMPLETED.value
        userRequest.description = "Total=${visitExports!!.size}"
        userRequest.attachment = attachment
        userRequest.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

        userRequestRepository.save(userRequest)

        notifyUserAfterExportingVisit(MFile(fileName, visitCsvFilePath), arrayOf(eUser.email!!)).let {
            if (it) {
                log("visitExportFile - Visit exported successfully and notified successfully!")
                userRequest.requestStatus = EnUserRequestStatus.SUCCESS.value
                userRequest.userNotified = true
                userRequest.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                userRequestRepository.save(userRequest)
            } else {
                log("visitExportFile - Failed to export and notify visit!")
            }
        }

        file.delete()

    }

    @Async(THREAD_POOL_TASK_EXECUTOR)
    fun visitExportFile(visitExports: ArrayList<VisitExport>?,
                        emailIds: Array<String>, ccEmailIds: Array<String>) {

        val fileName = "VisitData_${csvDateFormat()}.csv"
        val visitCsvFilePath = "${appProperty.filePath}$fileName"
        val writer = FileWriter(visitCsvFilePath)
        val sbc = StatefulBeanToCsvBuilder<VisitExport>(writer)
            .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
            .build()

        sbc.write(visitExports)
        writer.flush()
        writer.close()

        val file = File(visitCsvFilePath)

        notifyUserAfterExportingVisit(MFile(fileName, visitCsvFilePath), emailIds, ccEmailIds).let {
            if (!it) {
                log("visitExportFile - Failed to notify while exporting visit data")
            }
        }

        file.delete()

        log("visitExportFile - Visit exported successfully!")

    }

    fun notifyUserAfterExportingVisit(
        mFile: MFile,
        emailIds: Array<String>,
        cc: Array<String>? = null
    ) : Boolean {

        if (emailIds.isEmpty()) {
            return false
        }

        val sb = StringBuilder()
        sb.append("Hi,")
        sb.append("\n\nPlease find the below attached file with your requested visit data.")
        sb.append("\n\n\nThis is an auto generated email. Please do not reply.")
        sb.append("\n- Homefirst")

        mailHelper.sendMimeMessage(
            if (appProperty.isProduction() || appProperty.isStaging()) emailIds
            else arrayOf(SANJAY_JAISWAR_EMAIL, SANJAY_SHARMA_EMAIL),
            "Visit Export File",
            sb.toString(),
            false,
            arrayListOf(mFile),
            cc
        )

        println("notifyUserAfterExportingVisit - Mail sent successfully!")

        return true


    }

}