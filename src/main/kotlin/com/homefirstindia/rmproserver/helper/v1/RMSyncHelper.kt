package com.homefirstindia.rmproserver.helper.v1

import com.homefirstindia.rmproserver.dto.v1.RemarkDTO
import com.homefirstindia.rmproserver.model.v1.common.Reason
import com.homefirstindia.rmproserver.model.v1.common.Result
import com.homefirstindia.rmproserver.repository.v1.ReasonRepository
import com.homefirstindia.rmproserver.repository.v1.ResultRepository
import com.homefirstindia.rmproserver.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RMSyncHelper(
    @Autowired val reasonRepository: ReasonRepository,
    @Autowired val resultRepository: ResultRepository
) {
    fun addReasonData() {

        //collection related reason
        addReason("Courtesy meeting", COLLECTION)
        addReason("Payment collection", COLLECTION)
        addReason("Follow-up collection", COLLECTION)
        addReason("Meeting along with senior manager", COLLECTION)

        //============================********************====================//
        //connector related reason
        addReason("MBD visit", CONNECTOR)
        addReason("Document collection", CONNECTOR)
        addReason("Courtesy meeting", CONNECTOR)
        addReason("Meeting along with senior manager", CONNECTOR)
        addReason("Project visit", CONNECTOR)
        addReason("Follow-up on lead", CONNECTOR)
        addReason("Contest calling", CONNECTOR)
        addReason("Connector references", CONNECTOR)

        //============================********************====================//
        //lead related reason
        addReason("Customer to visit branch", LEAD)
        addReason("Counseling", LEAD)
        addReason("Verification", LEAD)
        addReason("Document collection", LEAD)

        //============================********************====================//

        //opportunity related reason
        addReason("Customer to visit branch", OPPORTUNITY)
        addReason("Property verification", OPPORTUNITY)
        addReason("Office verification", OPPORTUNITY)
        addReason("Residential verification", OPPORTUNITY)
        addReason("Processing Fee collection", OPPORTUNITY)
        addReason("Document collection", OPPORTUNITY)
        addReason("Legal and Technical", OPPORTUNITY)
        addReason("Meeting along with senior manager", OPPORTUNITY)
        addReason("Top-Up Request", OPPORTUNITY)

        //============================********************====================//
        //Branch visit related reason
        addReason("Branch visit", BRANCH_VISIT)

    }

    private fun addReason(name: String, type: String): Reason? {
        return try {

            val eReason = reasonRepository.findByNameAndType(name, type)

            eReason ?: run {
                println("Creating new reason: $name")
                reasonRepository.save(Reason().apply {
                    this.name = name
                    this.type = type
                    updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                })
            }

        } catch (e: Exception) {
            LoggerUtils.log("addReason - Error : ${e.message}")
            null
        }
    }

    fun addResultData() {

        //collection related results

        addResult("Another visit required", COLLECTION)
        addResult("Call not answered", COLLECTION)
        addResult("Door locked", COLLECTION)
        addResult("Need legal action", COLLECTION)
        addResult("Next follow-up date received", COLLECTION)
        addResult("Not Available", COLLECTION)
        addResult("Payment received - Full EMI", COLLECTION)
        addResult("Payment received - Partial EMI", COLLECTION)
        addResult("PTP received", COLLECTION)
        addResult("No PTP", COLLECTION)
        addResult("Requested a callback", COLLECTION)
        addResult("Senior visit required", COLLECTION)
        addResult("Switched Off / Not Reachable", COLLECTION)
        addResult("Visit done by the senior manager", COLLECTION)

        //============================********************====================//

        //connector related results
        addResult("Interested", CONNECTOR)
        addResult("Connector on-boarded", CONNECTOR)
        addResult("Referred another connector", CONNECTOR)
        addResult("Document collected", CONNECTOR)
        addResult("Visit done by the senior manager", CONNECTOR)
        addResult("Next follow-up date received", CONNECTOR)
        addResult("Call not answered", CONNECTOR)
        addResult("Reference shared", CONNECTOR)
        addResult("Not Interested", CONNECTOR)
        addResult("Not Available", CONNECTOR)
        addResult("Another visit required", CONNECTOR)

        //============================********************====================//

        //lead related results

        addResult("Customer visited", LEAD)
        addResult("Another visit required", LEAD)
        addResult("Customer did not visit", LEAD)
        addResult("Counseling done", LEAD)
        addResult("Call not answered", LEAD)
        addResult("Verification done", LEAD)
        addResult("Not Available", LEAD)
        addResult("Document collected", LEAD)
        addResult("Partial document collected", LEAD)
        addResult("Not Interested", LEAD)
        addResult("Partial verification done", LEAD)
        addResult("Requested a callback", LEAD)
        addResult("Next follow-up date received", LEAD)
        addResult("Interested", LEAD)

        //============================********************====================//
        //opportunity related results
        addResult("Verification done", OPPORTUNITY)
        addResult("Partial verification done", OPPORTUNITY)
        addResult("Another visit required", OPPORTUNITY)
        addResult("Not Available", OPPORTUNITY)
        addResult("Call not answered", OPPORTUNITY)
        addResult("Not Interested", OPPORTUNITY)
        addResult("Document collected", OPPORTUNITY)
        addResult("Partial document collected", OPPORTUNITY)
        addResult("Payment received", OPPORTUNITY)
        addResult("Visit done by the senior manager", OPPORTUNITY)
        addResult("Legal and Technical done", OPPORTUNITY)
        addResult("Top-Up created", OPPORTUNITY)
        addResult("Next follow-up date received", OPPORTUNITY)
        addResult("Requested a callback", OPPORTUNITY)
        addResult("Interested", OPPORTUNITY)

        //============================********************====================//
        //Branch visit related results
        addResult("Branch visit completed", BRANCH_VISIT)
        addResult("Another visit required", BRANCH_VISIT)
    }

    private fun addResult(name: String, type: String?): Result? {
        return try {

            val eResult = resultRepository.findByNameAndType(name, type)

            eResult ?: run {
                println("Creating new result: $name")
                resultRepository.save(Result().apply {
                    this.name = name
                    this.type = type
                    updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                })
            }

        } catch (e: Exception) {
            LoggerUtils.log("addResult - Error : ${e.message}")
            null
        }
    }
}