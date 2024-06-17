package com.homefirstindia.rmproserver.model.v1.common

import com.homefirstindia.rmproserver.utils.DateTimeUtils.getCurrentDateTimeInIST
import com.homefirstindia.rmproserver.utils.NA
import com.homefirstindia.rmproserver.utils.isNotNullOrNA
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "`Address`")
class Address {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var street: String? = null
    var city: String? = null
    var state: String? = null
    var country = "India"
    var postalCode: String? = null
    var mobile: String? = null
    var phone: String? = null
    var latitude: String? = null
    var longitude: String? = null


    @Column(columnDefinition = "JSON")
    var raw: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime = getCurrentDateTimeInIST()

    fun isValid(isMinimum: Boolean = false): Boolean {
        return if (isMinimum)
            hasValidLatLng(latitude) && hasValidLatLng(longitude)
        else
            hasData(city) && hasData(street) && hasData(state) && hasData(postalCode)
    }

    fun isValidStreet(): Boolean {
        return hasData(street)
    }

    fun isValidLatLng(): Boolean {
        return hasData(latitude) && hasData(longitude)
    }

    fun isPartiallyValid(): Boolean {
        return hasData(city) || hasData(street) || hasData(state) || hasData(postalCode)
    }

    fun shouldFetchAddress(): Boolean {
        return (!hasData(city) || !hasData(state)) && hasData(postalCode)
    }

    fun stateOrCityAvailable(): Boolean {
        return hasData(state) || hasData(city)
    }

    private fun hasData(value: String?): Boolean {
        return value.isNotNullOrNA()
    }

    private fun hasValidLatLng(value: String?): Boolean {
        return value.isNotNullOrNA() && value != 0.0.toString()
    }

    fun getFullAddress(): String {
        val sb = StringBuilder()
        if (street.isNotNullOrNA()) sb.append("$street, ")
        if (city.isNotNullOrNA()) sb.append("$city, ")
        if (state.isNotNullOrNA()) sb.append("$state, ")
        if (country.isNotNullOrNA()) sb.append("$country, ")
        if (postalCode.isNotNullOrNA()) sb.append(postalCode)
        if (sb.toString().isEmpty()) sb.append(NA)
        return sb.toString()
    }

    fun updateInfo(address: Address) {
        street = address.street
        city = address.city
        state = address.state
        postalCode = address.postalCode
        updateDatetime = getCurrentDateTimeInIST()
        latitude = address.latitude
        longitude = address.longitude
    }

}