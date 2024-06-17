package com.homefirstindia.rmproserver.helper.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.homefirstindia.rmproserver.dto.v1.RemarkDTO
import com.homefirstindia.rmproserver.networking.v1.CMSNetworkingClient
import com.homefirstindia.rmproserver.utils.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserProcessHelper(
    @Autowired val cmsNetworkClient: CMSNetworkingClient,
    @Autowired val objectMapper: ObjectMapper
) {

    private fun log(value: String) = LoggerUtils.log("UserProcessHelper.$value")

    fun addRemark(remarkDTO: RemarkDTO): LocalResponse {

        val localHTTPResponse = cmsNetworkClient.post(
                CMSNetworkingClient.Endpoints.ADD_REMARK.value,
                JSONObject(objectMapper.writeValueAsString(remarkDTO))
            )

        log("addRemark - CMS response: ${localHTTPResponse.stringEntity}")

        return LocalResponse().apply {
            message = localHTTPResponse.stringEntity
            isSuccess = localHTTPResponse.statusCode == 200
        }

    }
}