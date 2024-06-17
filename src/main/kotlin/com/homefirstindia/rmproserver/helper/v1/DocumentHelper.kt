package com.homefirstindia.rmproserver.helper.v1

import com.homefirstindia.rmproserver.manager.v1.AmazonClient
import com.homefirstindia.rmproserver.manager.v1.EnS3BucketPath
import com.homefirstindia.rmproserver.model.v1.common.Attachment
import com.homefirstindia.rmproserver.model.v1.visit.Visit
import com.homefirstindia.rmproserver.security.AppProperty
import com.homefirstindia.rmproserver.utils.*
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PdfMerger
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FileUtils
import org.apache.http.entity.ContentType
import org.apache.tika.Tika
import org.json.JSONArray
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream

@Component
class DocumentHelper(
    @Autowired val appProperty: AppProperty,
    @Autowired val amazonClient: AmazonClient,
    @Autowired val cryptoUtils: CryptoUtils,
) {

    private fun log(value : String) {
        LoggerUtils.log("DocumentHelper.$value")
    }

    private fun printLog(value : String) {
        println("DocumentHelper.$value")
    }

    fun convertFileFromBase64(base64: String, fileExtension: FileTypesExtentions) : File {

        val file = File("${appProperty.filePath}${System.currentTimeMillis()}${fileExtension.ext}")
        val pdfAsBytes: ByteArray = java.util.Base64.getDecoder().decode(base64)
        val os = FileOutputStream(file, false)
        os.write(pdfAsBytes)
        os.flush()
        os.close()

        return file

    }

    @Throws(Exception::class)
    fun mergePDF(fileData: JSONArray?): String? {

        val fileName = System.currentTimeMillis()
        val mergedFile = File("${appProperty.filePath}$fileName${FileTypesExtentions.PDF.ext}")

        // check that the data has at least one file
        if (fileData != null && fileData.length() > 0) {

            // create a new final pdf
            val pdfDoc = PdfDocument(PdfWriter(mergedFile))

            for (i in 0 until fileData.length()) {

                // decode the data and get the file type
                val decoder: ByteArray = Base64.decodeBase64(fileData.optString(i))
                val tika = Tika()

                val fileType = MimeMap.mapMimetoExt(tika.detect(decoder))

                // Use the bytes and create a temp file on local system
                val tempFileName = "${appProperty.filePath}temp$fileName$fileType"
                val tempPdfName = "${appProperty.filePath}temp$fileName.pdf"

                FileUtils.writeByteArrayToFile(File(tempFileName), decoder)
                var file: File
                var pdfDocument: PdfDocument?
                // if file is not a pdf then create a pdf as we can merge only multiple pdfs
                if (fileType !== MimeMap.PDF.extention) {
                    // create a temp PDF file and add the image to it
                    file = File(tempPdfName)
                    val pdfWriter = PdfWriter(file)
                    pdfDocument = PdfDocument(pdfWriter)
                    val document = Document(pdfDocument)
                    val imageData = ImageDataFactory.create(tempFileName)
                    val pdfImg = Image(imageData)
                    document.add(pdfImg.scaleToFit(PageSize.A4.width - 50, PageSize.A4.height))
                    document.close()
                    val imgFile = File(tempFileName)
                    imgFile.delete()
                } else {
                    // if file is a pdf then simply just pass it to the merger function
                    file = File(tempFileName)
                }

                // Merge the files
                val merger = PdfMerger(pdfDoc)
                val otherPdf = PdfDocument(PdfReader(file))
                merger.merge(otherPdf, 1, otherPdf.numberOfPages)
                file.delete()
                otherPdf.close()
            }
            pdfDoc.close()
        }

        // After all the files are merged return the Base64
        val fileContent: ByteArray = FileUtils.readFileToByteArray(
            File(appProperty.filePath + fileName + ".pdf")
        )
        val fileString = Base64.encodeBase64String(fileContent)
        mergedFile.delete()

        return fileString

    }

    fun uploadAttachment(
        fileName: String,
        file: MultipartFile,
        enS3BucketPath: EnS3BucketPath
    ): Attachment? {

        if (!listOf(
                ContentType.IMAGE_PNG.mimeType,
                ContentType.IMAGE_JPEG.mimeType
            ).contains(file.contentType)
        ) {
            val msg = "File format not supported. Please upload JPG, JPEG or PNG images"
            log("uploadAttachment - $msg")
            return null
        }

        val convertFile = convertMultiPartFileToFile(file, appProperty.filePath)

        convertFile ?: run {
            val msg = "failed to convert multipart file to file"
            log("uploadAttachment = - $msg")
            return null
        }

        val status = amazonClient.uploadFile(fileName, convertFile, enS3BucketPath)
        convertFile.delete()

        if (!status) {
            printLog("uploadAttachment - Failed to upload attachment to s3")
            return null
        }

       return Attachment().apply {
           this.fileName = fileName
           fileIdentifier = cryptoUtils.getFileIdentifier()
           contentType = ContentType.IMAGE_JPEG.mimeType
           attachmentType = "Image"
       }

    }

}