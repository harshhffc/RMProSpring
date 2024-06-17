package com.homefirstindia.rmproserver.manager.v1

import com.amazonaws.AmazonServiceException
import com.amazonaws.HttpMethod
import com.amazonaws.SdkClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.homefirstindia.rmproserver.model.v1.Creds
import com.homefirstindia.rmproserver.security.AppProperty
import com.homefirstindia.rmproserver.utils.CryptoUtils
import com.homefirstindia.rmproserver.utils.LoggerUtils.log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.net.URL
import java.util.*


@Configuration
class AmazonClient(
    @Autowired val appProperty: AppProperty,
    @Autowired private val credsManager: CredsManager,
    @Autowired val cryptoUtils: CryptoUtils,
) {

    private var _amazonCred: Creds? = null

    @Throws(Exception::class)
    private fun amazonCreds(): Creds {
        if (_amazonCred == null) {
            _amazonCred = credsManager.fetchCredentials(
                EnPartnerName.AMAZON, EnCredType.PRODUCTION
            )
            _amazonCred ?: throw Exception("Failed to get amazon credentials.")
        }
        return _amazonCred!!
    }

    @Bean
    fun s3(): AmazonS3 {

        if (amazonCreds().isEncrypted) {
            amazonCreds().username = cryptoUtils.decryptAes(amazonCreds().username)
            amazonCreds().password = cryptoUtils.decryptAes(amazonCreds().password)
        }

        val awsCredentials: AWSCredentials = BasicAWSCredentials(amazonCreds().username,
            amazonCreds().password)
        return AmazonS3ClientBuilder
            .standard()
            .withRegion(appProperty.s3BucketRegion)
            .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
            .build()
    }

    @Throws(Exception::class)
    fun uploadFile(fileName: String, file: File, bucketPath: EnS3BucketPath): Boolean {
        try {
            log("==> File saving in S3 with Name: $fileName")

            val putObjectRequest = PutObjectRequest(appProperty.s3BucketName,
                "${bucketPath.stringValue}/$fileName", file)
            s3().putObject(putObjectRequest)

            log("==> File saved successfully in S3 with Name: $fileName")

            return true
        } catch (e: AmazonServiceException) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace()
        } catch (e: SdkClientException) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace()
        }
        return false
    }

    @Throws(Exception::class)
    fun uploadFile(fileName: String, file: File, bucketName: String, bucketPath: EnS3BucketPath): Boolean {
        try {
            log("==> File saving in S3 with Name: $fileName")

            val putObjectRequest = PutObjectRequest(bucketName, "${bucketPath.stringValue}/$fileName", file)
            s3().putObject(putObjectRequest)

            log("==> File saved successfully in S3 with Name: $fileName")

            return true
        } catch (e: AmazonServiceException) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace()
        } catch (e: SdkClientException) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace()
        }
        return false
    }

    @Throws(Exception::class)
    fun getPublicURL(fileName: String, bucketPath: EnS3BucketPath, minutes: Int): String? {

        try {

            val expiration = Date()
            var expTimeMillis = expiration.time
            expTimeMillis += (1000 * 60 * minutes).toLong()
            expiration.time = expTimeMillis

            val preSignedUrlRequest = GeneratePresignedUrlRequest(
                appProperty.s3BucketName,
                bucketPath.stringValue + "/" + fileName
            ).withMethod(HttpMethod.GET).withExpiration(expiration)

            val url: URL = s3().generatePresignedUrl(preSignedUrlRequest)
            return url.toString()

        } catch (e: AmazonServiceException) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace()
        } catch (e: SdkClientException) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace()
        }

        return null
    }


}

enum class EnS3BucketPath(val stringValue: String) {
    VISIT("RMManagement/Visit"),
    VISIT_EXPORT("RMManagement/Visit/Export"),
    REPORT("RMManagement/Report"),
    LOGS_SERVER1("RMM/Server1"),
    LOGS_SERVER2("RMM/Server2")
}