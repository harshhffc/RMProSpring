package com.homefirstindia.rmproserver.helper.v1


import com.amazonaws.services.s3.model.PutObjectRequest
import com.homefirstindia.rmproserver.dto.v1.AdvanceFilter
import com.homefirstindia.rmproserver.manager.v1.AmazonClient
import com.homefirstindia.rmproserver.manager.v1.EnS3BucketPath
import com.homefirstindia.rmproserver.manager.v1.SalesforceManager
import com.homefirstindia.rmproserver.model.v1.common.AppUpdateInfo
import com.homefirstindia.rmproserver.repository.v1.AppUpdateRepository
import com.homefirstindia.rmproserver.repository.v1.SFSyncRepositoryMaster
import com.homefirstindia.rmproserver.repository.v1.UserRepository
import com.homefirstindia.rmproserver.security.AppProperty
import com.homefirstindia.rmproserver.services.v1.VisitService
import com.homefirstindia.rmproserver.utils.DateTimeFormat
import com.homefirstindia.rmproserver.utils.DateTimeUtils
import com.homefirstindia.rmproserver.utils.DateTimeZone
import com.homefirstindia.rmproserver.utils.LoggerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import javax.transaction.Transactional

@EnableAsync
@Component
class RMMScheduler(
    @Autowired val salesforceManager: SalesforceManager,
    @Autowired val sfSyncRepositoryMaster: SFSyncRepositoryMaster,
    @Autowired val sfSyncHelper: SFSyncHelper,
    @Autowired val userRepository: UserRepository,
    @Autowired val rmSyncHelper: RMSyncHelper,
    @Autowired val appProperty: AppProperty,
    @Autowired val visitService: VisitService,
    @Autowired val appUpdateRepository: AppUpdateRepository,
    @Autowired val amazonClient: AmazonClient
) {
    private fun log(value: String) = LoggerUtils.log("Scheduler.$value")

    @Transactional
//    @Scheduled(initialDelay = 1000, fixedDelay = Long.MAX_VALUE) //TODO: Comment for production
    @Scheduled(cron = "0 0 4 * * *", zone = "IST")  //TODO: Uncomment for production
    @Async
    fun cronBCRRUSync() {

        if (!appProperty.runScheduler)
            return

        log("cronBCRRUSync - Sync process started")

        println("cronBCRRUSync - Zone Sync Started")
//        sfSyncHelper.addZone() //TODO: Already added no need to run again and again
        println("cronBCRRUSync - Zone Sync Completed")

        println("cronBCRRUSync - Branch Sync Started")
        val jsonBranches = salesforceManager.getBranchInfo()
        val branches = sfSyncHelper.syncBranch(jsonBranches)
        sfSyncRepositoryMaster.branchRepository.saveAll(branches)
        println("cronBCRRUSync - Branch Sync Completed")

        println("cronBCRRUSync - Cluster Sync Started")
        val jsonClusters = salesforceManager.getClusterInfo()
        val clusters = sfSyncHelper.syncCluster(jsonClusters)
        sfSyncRepositoryMaster.clusterRepository.saveAll(clusters)
        println("cronBCRRUSync - Cluster Sync Completed")

        println("cronBCRRUSync - Region Sync Started")
        val jsonRegions = salesforceManager.getRegionInfo()
        val regions = sfSyncHelper.syncRegion(jsonRegions)
        sfSyncRepositoryMaster.regionRepository.saveAll(regions)
        println("cronBCRRUSync - Region Sync Completed")

        println("cronBCRRUSync - User Sync Started")
        val jsonUsers = salesforceManager.getUsers()
        val users = sfSyncHelper.syncUser(jsonUsers)
        sfSyncRepositoryMaster.userRepository.saveAll(users)
        println("cronBCRRUSync - User Sync Completed")

        println("cronBCRRUSync - Adding role")
//        sfSyncHelper.addAllRole() //TODO: Already added no need to run again and again
        println("cronBCRRUSync - Adding role completed")

        println("cronBCRRUSync - Adding reason and result")
//        rmSyncHelper.addReasonData() //TODO: Already added no need to run again and again
//        rmSyncHelper.addResultData() //TODO: Already added no need to run again and again
        println("cronBCRRUSync - Adding reason and result completed")

        println("cronBCRRUSync - Process role link started")
        sfSyncHelper.processRoleLink()
        println("cronBCRRUSync - Process role link completed")

        println("cronBCRRUSync - Process user map master started")
        sfSyncHelper.processUserMapMaster()
        println("cronBCRRUSync - Process user map master completed")

        println("cronBCRRUSync - Process zone map to map master started")
        sfSyncHelper.processZoneMapToMapMaster()
        println("cronBCRRUSync - Process zone map to map master completed")

        log("cronBCRRUSync - Sync process completed")

    }

    //    @Scheduled(initialDelay = 1000, fixedDelay = Long.MAX_VALUE) //TODO: Comment for production
    @Scheduled(cron = "0 0 9 * * MON", zone = "IST")  //TODO: Uncomment for production
    @Async
    fun cronExportVisit() {

        if (!appProperty.runScheduler)
            return

        log("cronExportVisit - weekly visit report started processing")

        val startDate = "${
            DateTimeUtils.getDateTimeByAddingDays(
                -7,
                DateTimeFormat.yyyy_MM_dd, DateTimeZone.IST
            )
        } 09:00:00"
        val endDate = "${DateTimeUtils.getCurrentDate()} 08:59:59"

        val advanceFilter = AdvanceFilter()
        advanceFilter.startDatetime = startDate
        advanceFilter.endDatetime = endDate

        visitService.exportVisit(advanceFilter).let {
            if (it) log("cronExportVisit - Visit Export Completed")
            else log("cronExportVisit - Failed to export visit")
        }

    }

//    @Scheduled(initialDelay = 1000, fixedDelay = Long.MAX_VALUE) //TODO: Comment for production
//    @Scheduled(cron = "0 5 12 * * *", zone = "IST")  //TODO: Uncomment for production
//    @Async
//    fun backUpLogs() {
//
//        if (!appProperty.backupLog)
//            return
//
//        try {
//
//            log("backUpLogs - process to move log files to S3")
//
////            val logsDir = if (appProperty.isProduction()) File("/usr/share/tomcat/logs")
////            else File(appProperty.filePath)
//
//            val containerName = "your_tomcat_container_name"  // Replace with your actual container name
//
//            val logsDirPath = "/usr/local/tomcat/logs"
//            val process = Runtime.getRuntime().exec("docker exec $containerName ls $logsDirPath")
//
//            val logsDir = File("/usr/local/tomcat/logs")
//
//            val totalLogs = logsDir.listFiles()!!.size
//            var totalProcessingLogs = 0
//            var totalProcessedLogs = 0
//
//            println("files ======${logsDir.listFiles()}")
//
//            for (logFile in logsDir.listFiles()!!) {
//
//                if (logFile.name.endsWith(".log")
//                    || logFile.name.endsWith(".txt")
//                ) {
//
//                    totalProcessingLogs++
//
//                    val fileName = logFile.name
//
//                    if (amazonClient.uploadFile(
//                            fileName, logFile,
//                            appProperty.s3LogBucketName,
//                            if (appProperty.runScheduler) EnS3BucketPath.LOGS_SERVER1 else EnS3BucketPath.LOGS_SERVER2
//                        )
//                    )
//                        totalProcessedLogs++
//
//                    logFile.delete()
//
////                    val date = getLogFileDate(logFile.name)
////
////                    if (date < DateTimeUtils.getCurrentDate()) {
////
////                        val fileName = logFile.name
////
////                        if (amazonClient.uploadFile(fileName, logFile,
////                                if (appProperty.isProduction()) appProperty.s3LogBucketName else appProperty.s3BucketName,
////                                if (appProperty.runScheduler) EnS3BucketPath.LOGS_SERVER1 else EnS3BucketPath.LOGS_SERVER2)
////                        )
////                            totalProcessedLogs++
////
////                        logFile.delete()
////
////                    }
//
//                }
//
//            }
//
//            log(
//                "backUpLogs - back up completed | total log: $totalLogs " +
//                        "| total processing log: $totalProcessingLogs | total processed log: $totalProcessedLogs"
//            )
//
//        } catch (e: Exception) {
//            log("backUpLogs - Error in backing logs: ${e.message}")
//        }
//
//    }


    @Scheduled(cron = "0 24 12 * * *", zone = "IST")  // TODO: Uncomment for production
    @Async
    fun backUpLogs() {

        if (!appProperty.backupLog)
            return

        try {
            log("backUpLogs - process to move log files to S3")

            val containerName = "rms"  // Replace with your actual container name
            val logsDirPath = "/usr/local/tomcat/logs"

            // List log files inside the container
            val listProcess = Runtime.getRuntime().exec("docker exec $containerName ls $logsDirPath")
            val logFiles = listProcess.inputStream.bufferedReader().readLines()

            var totalProcessingLogs = 0
            var totalProcessedLogs = 0

            for (fileName in logFiles) {
                if (fileName.endsWith(".log") || fileName.endsWith(".txt")) {
                    totalProcessingLogs++

                    // Read the log file from the container
                    val readProcess = Runtime.getRuntime().exec("docker exec $containerName cat $logsDirPath/$fileName")
                    val logContent = readProcess.inputStream.bufferedReader().readText()

                    // Create a temporary file to write the log content to
                    val tempFile = File.createTempFile(fileName, null)
                    tempFile.writeText(logContent)

                    if (amazonClient.uploadFile(
                            fileName, tempFile,
                            appProperty.s3LogBucketName,
                            if (appProperty.runScheduler) EnS3BucketPath.LOGS_SERVER1 else EnS3BucketPath.LOGS_SERVER2
                        )
                    ) {
                        totalProcessedLogs++
                    }

                    tempFile.delete()
                }
            }

            log(
                "backUpLogs - back up completed | total log: ${logFiles.size} " +
                        "| total processing log: $totalProcessingLogs | total processed log: $totalProcessedLogs"
            )

        } catch (e: Exception) {
            log("backUpLogs - Error in backing logs: ${e.message}")
        }
    }


    private fun getLogFileDate(name: String): String {
        return name.replace(".log", "")
            .replace(".txt", "")
            .replace("catalina.", "")
            .replace("localhost.", "")
            .replace("manager.", "")
            .replace("localhost_access_log.", "")
    }

}