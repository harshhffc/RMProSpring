package com.homefirstindia.rmproserver.manager.v1


import com.homefirstindia.rmproserver.networking.v1.SFConnection
import com.homefirstindia.rmproserver.utils.DateTimeFormat
import com.homefirstindia.rmproserver.utils.DateTimeUtils
import com.homefirstindia.rmproserver.utils.DateTimeZone
import com.homefirstindia.rmproserver.utils.LoggerUtils
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class SalesforceManager(
    @Autowired val sfConnection: SFConnection
) {

    private fun log(value: String) = LoggerUtils.log("v1/${this.javaClass.simpleName}.$value")

    @Throws(java.lang.Exception::class)
    fun getBranchInfo(): JSONObject? {
        val query = ("SELECT Branch_Address_line_1__c,Branch_Address_line_2__c,"
                + "Branch_City__c,Branch_Geo_Location__Latitude__s,Branch_Geo_Location__Longitude__s,"
                + "Branch_Pincode__c,Branch_Primary_Landline__c,Branch_Secondary_Landline__c,Branch_State__c,"
                + "Id,Name,BM_BMD__r.Id,BM_BMD__r.Name,BM_BMD__r.IsActive,Region__c , Cluster__c,"
                + "Region__r.Regional_Manager__r.Id,Region__r.Regional_Manager__r.Name,Region__r.Regional_Manager__r.IsActive,"
                + "Cluster__r.Cluster_Manager__r.Id,Cluster__r.Cluster_Manager__r.Name,Cluster__r.Cluster_Manager__r.IsActive,"
                + "Branch_Code__c,Branch_Status__c,HFFC_Physical_Branch__c FROM Branch__c order by Name")
        return sfConnection.get(query)
    }

    @Throws(java.lang.Exception::class)
    fun getClusterInfo(): JSONObject? {
        val query = ("SELECT Id, Name , Cluster_Code__c , Cluster_Manager__r.Name, Cluster_Manager__r.Id ," +
                " Region__c , Region__r.Regional_Manager__r.Name , Region__r.Regional_Manager__r.Id FROM Cluster__c order by Name")
        return sfConnection.get(query)
    }

    @Throws(java.lang.Exception::class)
    fun getRegionInfo(): JSONObject? {
        val query =
            ("SELECT Id, Name , Region_Code__c , Regional_Manager__r.Name, Regional_Manager__r.Id " +
                    "  FROM Region__c order by Name")
        return sfConnection.get(query)
    }

    @Throws(java.lang.Exception::class)
    fun getUsers(): JSONObject? {

        val query =
            ("SELECT Id, Name, Email, IsActive, Phone, MobilePhone,"
                    + " UserRole.Id, UserRole.Name, UserRole.ParentRoleId, LastModifiedDate"
                    + " FROM User"
                    + " WHERE UserRoleId != null"
                    + " order by Name")

        return sfConnection.get(query)

    }


}
