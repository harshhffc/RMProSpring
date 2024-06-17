package com.homefirstindia.rmproserver.manager.v1

import com.homefirstindia.rmproserver.model.v1.Creds
import com.homefirstindia.rmproserver.repository.v1.CredsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.persistence.EntityManager

enum class EnCredType(val value: String) {
    PRODUCTION("PRODUCTION"), UAT("UAT"), PRE_PROD("PRE_PROD");
}

enum class EnPartnerName(val value: String) {
    TELEGRAM("Telegram"),
    HFO_SPRING("HomefirstOneSpring"),
    GUPSHUP("Gupshup"),
    GOOGLE_MAPS("Google_Maps"),
    SALESFORCE("Salesforce"),
    TEAL("Teal"),
    TEAL_V2("Teal-V2"),
    AMAZON("AWS-RABIT"),
    GOOGLE_DNR("Google_DNR"),
    CMS("CMS");
}

@Component
class CredsManager(
    @Autowired private val credsRepository: CredsRepository,
    @Autowired private val entityManager: EntityManager
) {

    fun fetchCredentials(
        partnerName: EnPartnerName,
        credType: EnCredType
    ): Creds? {

        val cred =  credsRepository.findByPartnerNameAndCredType(
            partnerName.value,
            credType.value
        )?.apply {
            entityManager.detach(this)
        }

        return cred

    }

}