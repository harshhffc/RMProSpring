package com.homefirstindia.rmproserver.model.v1

import com.homefirstindia.rmproserver.model.v1.common.Address
import com.homefirstindia.rmproserver.utils.DateTimeUtils
import com.homefirstindia.rmproserver.utils.isNotNullOrNA
import com.homefirstindia.rmproserver.utils.isValid
import org.hibernate.annotations.GenericGenerator
import org.json.JSONObject
import javax.persistence.*


@Entity
@Table(name = "`Branch`")
class Branch() {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var sfId: String? = null
    var name: String? = null
    var primaryNumber: String? = null
    var secondaryNumber: String? = null
    var latitude: String? = null
    var longitude: String? = null

    var sfRegionId: String? = null
    var sfClusterId: String? = null

    var branchManager: String? = null
    var branchManagerSfId: String? = null

    var clusterManager: String? = null
    var clusterManagerSfId: String? = null

    var regionalManager: String? = null
    var regionalManagerSfId: String? = null

    var branchCode: String? = null
    var branchStatus: String? = null

    var physicalEstablishment: String? = null
    var branchKey: String? = null

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "addressId", referencedColumnName = "id")
    var address: Address? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    constructor(sfJson: JSONObject?) : this() {

        if (null == sfJson) return

        sfId = sfJson.optString("Id")
        sfRegionId = sfJson.optString("Region__c")
        sfClusterId = sfJson.optString("Cluster__c")
        name = sfJson.optString("Name")
        branchKey = sfJson.optString("Name")
        primaryNumber = sfJson.optString("Branch_Primary_Landline__c")
        secondaryNumber = sfJson.optString("Branch_Secondary_Landline__c")
        latitude = sfJson.optString("Branch_Geo_Location__Latitude__s")
        longitude = sfJson.optString("Branch_Geo_Location__Longitude__s")

        address = Address().apply {

            val addressLine1 = sfJson.optString("Branch_Address_line_1__c")
            val addressLine2 = sfJson.optString("Branch_Address_line_2__c")

            street = addressLine1.toString() + if (addressLine2.isNotNullOrNA()) addressLine2 else ""

            postalCode = sfJson.optString("Branch_Pincode__c")
            city = sfJson.optString("Branch_City__c")
            state = sfJson.optString("Branch_State__c")

            if (this@Branch.latitude.isNotNullOrNA())
                latitude = this@Branch.latitude

            if (this@Branch.longitude.isNotNullOrNA())
                longitude = this@Branch.longitude

        }

        branchCode = sfJson.optString("Branch_Code__c")
        branchStatus = sfJson.optString("Branch_Status__c")
        physicalEstablishment = sfJson.optString("HFFC_Physical_Branch__c")

        sfJson.optJSONObject("BM_BMD__r")?.let { bJson ->
            branchManager = bJson.optString("Name")
            branchManagerSfId = bJson.optString("Id")
        }

        sfJson.optJSONObject("Cluster__r")?.let { bJson ->
            bJson.optJSONObject("Cluster_Manager__r")?.let { cmJson ->

                clusterManager = cmJson.optString("Name")
                clusterManagerSfId = cmJson.optString("Id")
            }

        }

        val rJson = sfJson.optJSONObject("Region__r")

        if (null != rJson) {
            val regionJson = rJson.optJSONObject("Regional_Manager__r")
            if (null != regionJson) {
                regionalManager = regionJson.optString("Name")
                regionalManagerSfId = regionJson.optString("Id")
            }
        }

    }

}

@Entity
@Table(name = "`Cluster`")
class Cluster() {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var sfId: String? = null
    var sfRegionId: String? = null
    var name: String? = null
    var code: String? = null

    var clusterManager: String? = null
    var clusterManagerSfId: String? = null

    var regionalManager: String? = null
    var regionalManagerSfId: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    constructor(sfJson: JSONObject?) : this() {

        if (null == sfJson) return

        sfId = sfJson.optString("Id")
        sfRegionId = sfJson.optString("Region__c")
        name = sfJson.optString("Name")
        code = sfJson.optString("Cluster_Code__c")

        sfJson.optJSONObject("Cluster_Manager__r")?.let { bJson ->
            clusterManager = bJson.optString("Name")
            clusterManagerSfId = bJson.optString("Id")
        }

        sfJson.optJSONObject("Region__r")?.let { bJson ->
            bJson.optJSONObject("Regional_Manager__r")?.let { cmJson ->

                regionalManager = cmJson.optString("Name")
                regionalManagerSfId = cmJson.optString("Id")
            }

        }
    }


}


@Entity
@Table(name = "`Region`")
class Region() {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var sfId: String? = null
    var name: String? = null
    var code: String? = null

    var zonalManager: String? = null
    var zonalManagerSfId: String? = null
    var zoneId: String? = null

    var regionalManager: String? = null
    var regionalManagerSfId: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    constructor(sfJson: JSONObject?) : this() {

        if (null == sfJson) return

        sfId = sfJson.optString("Id")
        name = sfJson.optString("Name")
        code = sfJson.optString("Region_Code__c")

        val regionJson = sfJson.optJSONObject("Regional_Manager__r")
        if (null != regionJson) {
            regionalManager = regionJson.optString("Name")
            regionalManagerSfId = regionJson.optString("Id")
        }
    }

}

@Entity
@Table(name = "`Zone`")
class Zone {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null
    var name: String? = null
    var zonalManager: String? = null
    var sfId: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()
}

@Entity
@Table(name = "`rmm_UserMapMaster`")
class UserMapMaster {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var userId: String? = null
    var bmId: String? = null
    var clumId: String? = null
    var rgmId: String? = null
    var zmId: String? = null
    var branchId: String? = null
    var clusterId: String? = null
    var regionId: String? = null
    var zoneId: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

}