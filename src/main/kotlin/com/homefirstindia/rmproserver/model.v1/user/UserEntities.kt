package com.homefirstindia.rmproserver.model.v1.user

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.homefirstindia.rmproserver.model.v1.common.Attachment
import com.homefirstindia.rmproserver.utils.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.json.JSONObject
import javax.persistence.*

@Entity
@Table(name = "`user`")
class User() {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id = -1

    @Column(name = "sf_user_id", nullable = false)
    var sfUserId: String? = null

    @JsonIgnore
    @Column(name = "org_id")
    var orgId: String? = null

    @Column(name = "first_name")
    var firstName: String? = null

    @Column(name = "last_name")
    var lastName: String? = null

    @Column(name = "display_name")
    var displayName: String? = null

    var email: String? = null

    @Column(name = "mobile_number")
    var mobileNumber: String? = null

    var username: String? = null

    @JsonIgnore
    @Column(name = "id_url")
    var idUrl: String? = null

    @Column(name = "sf_role_id")
    var sfUserRoleId: String? = null

    @Column(name = "sf_role_name")
    var sfUserRoleName: String? = null

    @Column(name = "sf_parent_role_id")
    var sfParentRoleId: String? = null

    @JsonIgnore
    @Column(name = "device_id")
    var deviceId: String? = null

    @JsonIgnore
    @Column(name = "device_type")
    var deviceType: String? = null

    @JsonIgnore
    @Column(name = "register_datetime", columnDefinition = "DATETIME", updatable = false)
    var registerDatetime: String? = null

    @Column(name = "update_datetime", columnDefinition = "DATETIME")
    var updateDatetime: String? = null

    @JsonIgnore
    @Column(name = "last_login_datetime", columnDefinition = "DATETIME")
    var lastLoginDatetime: String? = null

    @Column(name = "session_passcode")
    var sessionPasscode: String? = null

    @JsonIgnore
    @Column(name = "sf_is_active", columnDefinition = "BOOLEAN default false")
    var isActive = false

    @JsonIgnore
    @Column(name = "is_role_updatable", columnDefinition = "BOOLEAN default true")
    var isRoleUpdatable = true

    @JsonIgnoreProperties("users")
    @ManyToMany
    @JoinTable(
        name = "rmm_users_roles",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")]
    )

    @LazyCollection(LazyCollectionOption.FALSE)
    var roles: Collection<Role>? = null

    constructor(sfJson: JSONObject?) : this() {

        if (null == sfJson) return

        sfUserId = sfJson.optString("Id")
        displayName = sfJson.optString("Name")

        firstName = displayName.getFirstName()
        lastName = displayName.getLastName()

        sfJson.optString("MobilePhone")?.let {
            if (!it.isInvalid())
                mobileNumber = getTruncatedDataFromEnd(it.replace("\\s".toRegex(), ""), 10)
        }

        sfJson.optJSONObject("UserRole")?.let { ur ->
            sfUserRoleId = ur.optString("Id")
            sfUserRoleName = ur.optString("Name")
            sfParentRoleId = ur.optString("ParentRoleId")
        }

        email = sfJson.optString("Email")

        isActive = sfJson.optBoolean("IsActive", false)

        updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    }

    fun updateUser(eUser: User) {

        id = eUser.id
        orgId = eUser.orgId
        username = eUser.username
        idUrl = eUser.idUrl
        deviceId = eUser.deviceId
        deviceType = eUser.deviceType
        registerDatetime = eUser.registerDatetime
        lastLoginDatetime = eUser.lastLoginDatetime
        sessionPasscode = eUser.sessionPasscode
        updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

        if (!eUser.isRoleUpdatable) {
            sfParentRoleId = eUser.sfParentRoleId
            sfUserRoleId = eUser.sfUserRoleId
            sfUserRoleName = eUser.sfUserRoleName
            isRoleUpdatable = false
        }

    }

}

@Entity
@Table(name = "`rmm_Role`")
class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    var name: String? = null

    var description: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false)
    var createDatetime: String? = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME", updatable = false)
    var updateDatetime: String? = null

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    var users: Collection<User>? = null

}

@Entity
@Table(name = "`rmm_BranchNameKeyMap`")
class BranchNameKeyMap {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var branchName: String? = null

    var branchKey: String? = null

}

@Entity
@Table(name = "`rmm_UserLog`")
class UserLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    @Column(nullable = false)
    var userId: String? = null

    @Column(nullable = false)
    var userAction: String? = null

    @Column(length = 512)
    var actionDesc: String? = null

    var objectId: String? = null
    var objectName: String? = null

    var methodName: String? = null

    var ipAddress: String? = null

    var deviceId: String? = null
    var deviceType: String? = null
    var deviceName: String? = null

    var appVersion: String? = null

    var requestStatus: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var logDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

}

@Entity
@Table(name = "`rmm_UserRequest`")
class UserRequest {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var userName: String? = null

    @Column(nullable = false)
    var requestType: String? = null

    @Column(nullable = false)
    var userId = NA

    var email: String? = null

    var requestStatus: String? = null

    @Column(name="rawRequest", columnDefinition="JSON")
    var rawRequest: String? = null

    var description = NA

    @ColumnDefault("0")
    var isProcessed = false

    @ColumnDefault("0")
    var userNotified = false

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "attachmentId", referencedColumnName = "id")
    var attachment: Attachment? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String? = DateTimeUtils.getCurrentDateTimeInIST()

}