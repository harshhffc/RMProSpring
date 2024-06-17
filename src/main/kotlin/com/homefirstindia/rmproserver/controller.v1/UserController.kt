package com.homefirstindia.rmproserver.controller.v1

import com.homefirstindia.rmproserver.dto.v1.RemarkDTO
import com.homefirstindia.rmproserver.services.v1.UserService
import com.homefirstindia.rmproserver.utils.LoggerUtils
import com.homefirstindia.rmproserver.utils.OneResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/user/v1")
class UserController(
    @Autowired val oneResponse: OneResponse,
    @Autowired val userService: UserService) {

    private fun logMethod(value: String) = LoggerUtils.logMethodCall("/user/v1/$value")

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    @GetMapping(
        "/getMetaData",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun ucGetUserMetaData(
        @RequestHeader userSourceId: Int
    ): ResponseEntity<String>? {

        log("ucGetUserMetaData")

        return try {
            userService.getMetaData(userSourceId)
        } catch (e:Exception) {
            log("ucGetUserMetaData - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @GetMapping(
        "/getAppUpdateInfo",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun acGetAppUpdateInfo(): ResponseEntity<String> {

        logMethod("ucGetAppUpdateInfo")

        return try {
            userService.getAppUpdateInfo()
        } catch (e: Exception) {
            log("ucGetAppUpdateInfo - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

    @PostMapping(
        "/remark.add",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun epAddRemark(
        @RequestHeader userSourceId: Int,
        @RequestBody remarkDTO: RemarkDTO,
    ): ResponseEntity<String>? {

        logMethod("ucAddRemark")

        return try {
            userService.addRemark(userSourceId, remarkDTO)
        } catch (e: Exception) {
            log("ucAddRemark - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }
    }

    @PostMapping(
        "/remark.all",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun ucGetAllRemark(
        @RequestHeader userSourceId: Int,
        @RequestBody remarkDTO: RemarkDTO,
    ): ResponseEntity<String>? {

        logMethod("ucGetAllRemark")

        return try {
            userService.getAllRemark(userSourceId, remarkDTO)
        } catch (e: Exception) {
            log("ucGetAllRemark - Error : ${e.message}")
            e.printStackTrace()
            oneResponse.defaultFailureResponse
        }

    }

}