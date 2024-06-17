package com.homefirstindia.rmproserver.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.rmproserver.dto.v1.AdvanceFilter
import com.homefirstindia.rmproserver.dto.v1.LocationDirections
import com.homefirstindia.rmproserver.dto.v1.LocationDistanceMatrix
import com.homefirstindia.rmproserver.model.v1.visit.Visit
import com.homefirstindia.rmproserver.security.AppProperty
import com.homefirstindia.rmproserver.services.v1.VisitService
import com.homefirstindia.rmproserver.utils.LoggerUtils
import com.homefirstindia.rmproserver.utils.OneResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/visit/v1")
class VisitController(
    @Autowired val oneResponse: OneResponse,
    @Autowired val visitService: VisitService,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val appProperty: AppProperty
) {

    private fun logMethod(value: String) = LoggerUtils.logMethodCall("/visit/v1/$value")

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    @GetMapping(
        "/getMetaData",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getMetaData(): ResponseEntity<String> {
        return try {
            visitService.getMetaData()
        } catch (e: Exception) {
            log("getMetaData - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/getDashboard",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getDashboard(
        @RequestHeader userSourceId: Int,
        @RequestBody advanceFilter: AdvanceFilter,
    ): ResponseEntity<String> {
        return try {
            visitService.getDashboardData(userSourceId, advanceFilter)
        } catch (e: Exception) {
            log("getDashboard - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/add",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun addVisit(
        @RequestHeader userSourceId: Int,
        @RequestParam("image") image: MultipartFile?,
        @RequestParam("visit") visit: String
    ): ResponseEntity<String> {

        logMethod("addVisit")

        return try {
            visitService.addVisit(
                userSourceId,
                image,
                objectMapper.readValue(visit, Visit::class.java)
            )
        } catch (e: Exception) {
            log("addVisit - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PatchMapping(
        "/update/{id}",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun updateVisit(
        @RequestHeader userSourceId: Int,
        @RequestParam("image") image: MultipartFile?,
        @RequestParam("visit") visitDetail: String
    ): ResponseEntity<String> {
        logMethod("updateVisit")

        return try {
            visitService.updateVisit(
                userSourceId,
                image,
                objectMapper.readValue(visitDetail, Visit::class.java)
            )
        } catch (e: Exception) {
            log("updateVisit - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/detail/{id}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getDetail(
        @RequestHeader userSourceId: Int,
        @PathVariable("id") id: String
    ): ResponseEntity<String> {
        return try {
            return visitService.getDetail(id)
        } catch (e: Exception) {
            log("getDetail - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/getAll",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getVisits(
        @RequestHeader userSourceId: Int,
        @RequestBody advanceFilter: AdvanceFilter,
        pageable: Pageable
    ): ResponseEntity<String> {
        return try {
            return visitService.getVisits(userSourceId, advanceFilter, pageable)
        } catch (e: Exception) {
            log("getVisits - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/getDistanceMatrix",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getDistanceMatrix(
        @RequestHeader userSourceId: Int,
        @RequestBody locationDistanceMatrix: LocationDistanceMatrix
    ): ResponseEntity<String> {

        return try {

            return visitService.getDistanceMatrix(
                userSourceId,
                locationDistanceMatrix
            )

        } catch (e: Exception) {

            log("getDistanceMatrix - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse

        }

    }

    @PostMapping(
        "/getDirections",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getDirections(
        @RequestHeader userSourceId: Int,
        @RequestBody locationDirections: LocationDirections
    ): ResponseEntity<String> {

        return try {

            return visitService.getDirections(
                userSourceId,
                locationDirections
            )

        } catch (e: Exception) {

            log("getDirections - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse

        }
    }

    @PostMapping(
        "/export",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun exportVisitData(
        @RequestHeader userSourceId: Int,
        @RequestBody advanceFilter: AdvanceFilter,
    ): ResponseEntity<String> {
        return try {
            visitService.exportVisit(userSourceId, advanceFilter)
        } catch (e: Exception) {
            log("exportVisitData - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/addressLookup",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getRelatedAddress(
        @RequestHeader userSourceId: Int,
        @RequestParam("mobileNumber") mobileNumber: String,
        @RequestParam("objectId") objectId: String,
        @RequestParam("objectType") objectType: String,
        @RequestParam("addressType") addressType: String
    ): ResponseEntity<String> {
        return try {
            visitService.getRelatedAddress(userSourceId, mobileNumber, objectId, objectType, addressType)
        } catch (e: Exception) {
            log("getRelatedAddress - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/getAllByObject",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getAllVisitByObject(
        @RequestHeader userSourceId: Int,
        @RequestParam("objectId") objectId: String,
    ): ResponseEntity<String> {
        return try {
            return visitService.getAllVisitByObject(userSourceId, objectId)
        } catch (e: Exception) {
            log("getAllVisitByObject - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

}
