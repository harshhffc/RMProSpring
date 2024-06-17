package com.homefirstindia.rmproserver.utils

import org.apache.http.entity.ContentType

const val ORG_ID = "orgId"
const val AUTHORIZATION = "Authorization"
const val VALID_UPTO = "validUpto"

const val SESSION_PASSCODE = "sessionPasscode"
const val SOURCE_PASSCODE = "sourcePasscode"
const val CROWN_PASSCODE = "crownPasscode"
const val USER_SOURCE_ID = "userSourceId"
const val DEFAULT_ERROR_MESSAGE = "Something went wrong.Please try again!"
const val NA = "NA"
const val STATUS = "status"
const val SUCCESS = "success"
const val FAILURE = "failure"
const val MESSAGE = "message"
const val ERROR = "error"
const val ACTION = "action"
const val ID = "id"
const val TOKEN = "token"
const val USER = "user"
const val USER_ROLE = "userRoles"
const val REFERSH_TOKEN = "refreshToken"
const val NO_DECIMAL_NUMBER = -1.0
const val NO_NUMBER = -1
const val THREAD_POOL_TASK_EXECUTOR = "threadPoolTaskExecutor"

const val BD_COUNT = "bdCount"
const val MESSENGER_GROUPS = "messengerGroups"

const val CONNECTOR_DORMANT_ACTIVITY_KEY = "Dormant Connector"
const val YES = "Yes"
const val ROLES = "roles"
const val TEAMS = "teams"
const val AUTHORIZED = "AUTHORIZED"

const val CONNECTOR = "CONNECTOR"
const val COLLECTION = "COLLECTION"
const val PROPERTY = "PROPERTY"
const val LEAD = "LEAD"
const val OPPORTUNITY = "OPPORTUNITY"
const val BUILDER = "BUILDER"
const val COMPLETED = "COMPLETED"
const val PENDING = "PENDING"
const val ALL = "ALL"
const val USERS = "USER"
const val BRANCH = "BRANCH"
const val CLUSTER = "CLUSTER"
const val REGION = "REGION"
const val ZONE = "ZONE"
const val BRANCH_VISIT = "BRANCH_VISIT"

const val MY_VIEW = "MY VIEW"

const val CONTENT_TYPE_APPLICATION_JSON = "application/json"

const val ROLE_SUPER_ADMIN = "SUPER_ADMIN"
const val ROLE_ADMIN = "ADMIN"
const val ROLE_MANAGEMENT = "MANAGEMENT"
const val ROLE_HO_USER = "HO_USER"
const val ROLE_RGM = "RGM"
const val ROLE_ZM = "ZM"
const val ROLE_CM = "CM"
const val ROLE_BM = "BM"
const val ROLE_RM = "RM"
const val ROLE_CSM = "CSM"
const val ROLE_CALL_CENTER_AGENT = "CALL_CENTER_AGENT"
const val KEY_ROLE_CLUM = "CLUM"
const val KEY_ROLE_RGM = "Regional Manger"
const val KEY_ROLE_RM = "Relationship Manager"
const val KEY_ROLE_BM = "Branch Manager"
const val KEY_ROLE_BRANCH = "Branch"

const val SANJAY_JAISWAR_EMAIL = "sanjay.jaiswar@homefirstindia.com"
const val SANJAY_SHARMA_EMAIL = "sanjay.sharma@homefirstindia.com"
const val RANAN_RODRIGUES_EMAIL = "ranan.rodrigues@homefirstindia.com"
const val AJAY_KHETAN_EMAIL = "ajay.khetan@homefirstindia.com"

const val CMS = "CMS"
const val SOURCE_RM_PRO = "RM_PRO"
const val REMARKS = "remarks"

enum class Actions(val value: String) {
    AUTHENTICATE_AGAIN("AUTHENTICATE_AGAIN"),
    RETRY("RETRY"),
    FIX_RETRY("FIX_RETRY"),
    CANCEL("CANCEL"),
    CONTACT_ADMIN("CONTACT_ADMIN"),
    DO_REGISTRATION("DO_REGISTRATION"),
    DO_VERIFICATION("DO_VERIFICATION"),
    GO_BACK("GO_BACK"),
    DO_LOGIN("DO_LOGIN"),
    DO_LOOKUP("DO_LOOKUP"),
    CONTINUE("CONTINUE");
}

enum class Errors(val value: String) {
    UNKNOWN("UNKNOWN"),
    FAILED("FAILED"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND"),
    ACCESS_DENIED("ACCESS_DENIED"),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS"),
    DUPLICATE_RECORD("DUPLICATE_RECORD"),
    STRING_TOO_LONG("STRING_TOO_LONG"),
    JSON_PARSER_ERROR("JSON_PARSER_ERROR"),
    OPERATION_FAILED("OPERATION_FAILED"),
    INVALID_DATA("INVALID_DATA"),
    INVALID_REQUEST("INVALID_REQUEST"),
    INCONLUSIVE("INCONLUSIVE"),
    SERVICE_NOT_FOUND("SERVICE_NOT_FOUND");
}

enum class EnUserRequestStatus(val value: String) {
    CREATED("CREATED"),
    FAILED("FAILED"),
    PARTIALLY_COMPLETED("PARTIALLY_COMPLETED"),
    SUCCESS("SUCCESS");
}

enum class AttachmentType(val value: String) {
    RM_PRO_VISIT_EXPORT("RMPROVisitExport"),
}

enum class EnMessengerType(val value: String) {
    CONNECTOR("CONNECTOR");

    companion object {
        operator fun get(value: String): EnMessengerType {
            return values().first { it.value == value }
        }
    }
}

enum class EnConnectorType(val key: String, val displayName: String) {
    EXISTING_CONNECTOR("EXISTING_CONNECTOR", "Existing Connector"),
    NEW_CONNECTOR("NEW_CONNECTOR", "New Connector"),
    BUILDER("BUILDER", "Builder");

    companion object {
        operator fun get(key: String): EnConnectorType {
            return values().first { it.key == key }
        }
    }
}

enum class EnConnectorMode(val key: String, val displayName: String) {
    PHONE_CALL("PHONE_CALL", "Phone Call"),
    PHYSICAL_VISIT("PHYSICAL_VISIT", "Physical Visit");

    companion object {
        operator fun get(key: String): EnConnectorMode {
            return values().first { it.key == key }
        }
    }
}

enum class EnConnectorResult(val key: String, val displayName: String) {
    INTERESTED("INTERESTED", "Interested"),
    NOT_INTERESTED("NOT_INTERESTED", "Not Interested"),
    LEAD_SHARED("LEAD_SHARED", "Lead Shared"),
    VISIT_REQUIRED("VISIT_REQUIRED", "Another Visit Required"),
    VISIT_REQUIRED_SENIOR("VISIT_REQUIRED_SENIOR", "Another Visit Required with Senior");

    companion object {
        operator fun get(key: String): EnConnectorResult {
            return values().first { it.key == key }
        }
    }
}

enum class EnPostStatus(val value: String) {
    INITIATED("INITIATED"),
    FAILED("FAILED"),
    SUCCESS("SUCCESS");

    companion object {
        operator fun get(value: String): EnPostStatus {
            return values().first { it.value == value }
        }
    }
}

enum class EnServiceType(val value: String) {

    TELEGRAM("Telegram");

    companion object {
        operator fun get(value: String): EnServiceType {
            return values().first { it.value == value }
        }
    }

}

enum class EnMyObject(val value: String) {
    MESSGENGER_POST("MessengerPost"),
    VISIT("Visit")
}

enum class FileTypesExtentions(val ext: String, val contentType: String, val displayName: String) {
    PDF(".pdf", "application/pdf", "PDF"),
    HTML(".html", "text/html", "HTML"),
    MP3(".mp3", "audio/mpeg", "MP3"),
    CSV(".csv", "text/csv", "CSV"),
    IMAGE(".jpeg", "image/jpeg", "Image");

    companion object {
        fun getExtFromType(ext: String): String? {
            for (item: FileTypesExtentions in values()) {
                if ((item.contentType == ext)) return item.ext
            }
            return null
        }

        operator fun get(value: String): FileTypesExtentions? {
            for (ext: FileTypesExtentions in FileTypesExtentions.values()) {
                if ((ext.ext == value)) return ext
            }
            return null
        }

        fun imageFormats() = listOf(
            ContentType.IMAGE_JPEG.mimeType,
            ContentType.IMAGE_PNG.mimeType
        )
    }
}

enum class RoleType(val key: String, val displayName: String) {
    CSM("CSM", "Customer Service Manager"),
    RM("RM", "Relationship Manager"),
    BM("BM", "Branch Manager"),
    CM("CM", "Cluster Manager"),
    RGM("RGM", "Regional Manager"),
    ZM("ZM", "Zonal Manager"),
    HO("HO", "Head Office");

    companion object {
        operator fun get(key: String): RoleType {
            return RoleType.values().first { it.key == key }
        }
    }
}

enum class EnObjectType(val value: String, val displayName: String) {
    ALL("ALL", "All"),
    CONNECTOR("CONNECTOR", "Connector"),
    COLLECTION("COLLECTION", "Collection"),
    OPPORTUNITY("OPPORTUNITY", "Opportunity"),
    LEAD("LEAD", "Lead"),
    BRANCH_VISIT("BRANCH_VISIT", "Branch Visit");

    companion object {
        operator fun get(value: String): EnObjectType? {
            return EnObjectType.values().firstOrNull { it.value == value }
        }

        fun getAllObjectType(): ArrayList<String> {

            return ArrayList<String>().apply {
                this.add(CONNECTOR.value)
                this.add(COLLECTION.value)
                this.add(LEAD.value)
                this.add(OPPORTUNITY.value)
                this.add(BRANCH_VISIT.value)
            }
        }
    }
}

enum class EnVisitStatus(val value: String) {
    ALL("ALL"),
    PENDING("PENDING"),
    COMPLETED("COMPLETED")
}

enum class EnRole(val value: String) {
    SUPER_ADMIN(ROLE_SUPER_ADMIN),
    ADMIN(ROLE_ADMIN),
    MANAGEMENT(ROLE_MANAGEMENT),
    HO_USER(ROLE_HO_USER),
    ZM(ROLE_ZM),
    RGM(ROLE_RGM),
    CM(ROLE_CM),
    BM(ROLE_BM),
    RM(ROLE_RM),
    CSM(ROLE_CSM);

    companion object {

        val superRoles = arrayListOf(SUPER_ADMIN, ADMIN, MANAGEMENT)

        fun isTeamRequired(role: String): Boolean = !superRoles.contains(valueOf(role))
    }
}

enum class MyObject(val value: String) {

    LEAD("Lead"),
    COLLECTION("Collection"),
    USER("User"),
    ROLE("Role"),
    USER_REQUEST("rmm_UserRequest"),
    RMM_CREDENTIAL("RMMCredential"),
    VISIT("Visit");

    companion object {
        @Throws(Exception::class)
        operator fun get(value: String): MyObject {
            for (mo: MyObject in values()) {
                if ((mo.value == value)) return mo
            }
            throw Exception("No Object mapped for value: $value")
        }
    }
}

enum class EnDashlet(val value: String, val displayName: String) {
    ALL("ALL", "All"),
    CONNECTOR("CONNECTOR", "Connector Visit"),
    COLLECTION("COLLECTION", "Collection Visit"),
    LEAD("LEAD", "Lead Visit"),
    OPPORTUNITY("OPPORTUNITY", "Opportunity Visit"),
    BRANCH_VISIT("BRANCH_VISIT", "Branch Visit"),
    USER("USER", "User"),
    ZONE("ZONE", "Zone"),
    REGION("REGION", "Region"),
    CLUSTER("CLUSTER", "Cluster"),
    BRANCH("BRANCH", "Branch"),
    MY_VIEW("MY_VIEW", "Visit View");

    companion object {
        operator fun get(value: String): EnDashlet? {
            return EnDashlet.values().firstOrNull { it.value == value }
        }
    }
}

enum class EnFilterType(val key: String, val value: String) {
    VISIT_TYPE("VISIT_TYPE", "Visit Type"),
    ZONE("ZONE", "Zone"),
    REGION("REGION", "Region"),
    CLUSTER("CLUSTER", "Cluster"),
    BRANCH("BRANCH", "Branch"),
    USER("USER", "User"),
}

enum class EnAddressType(val value: String) {
    ORIGIN("ORIGIN"),
    DESTINATION("DESTINATION");

    companion object {
        @Throws(Exception::class)
        operator fun get(value: String): EnAddressType {
            for (add in EnAddressType.values()) {
                if ((add.value == value)) return add
            }
            throw Exception("No Address type match for value: $value")
        }
    }
}

enum class CredType(val value: String) {
    PRODUCTION("PRODUCTION"), UAT("UAT");
}
