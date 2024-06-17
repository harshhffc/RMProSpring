package com.homefirstindia.rmproserver.model.v1.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.persistence.AttributeConverter
import javax.persistence.Converter


@Converter
class FeaturesConverter : AttributeConverter<List<String?>?, String?> {
    override fun convertToDatabaseColumn(attribute: List<String?>?): String? {
        try {
            return gson.toJson(attribute)
        } catch (e: Exception) {
            println("FeaturesConverter: Exception while convertToDatabaseColumn: $e")
        }
        return null
    }

    override fun convertToEntityAttribute(dbData: String?): List<String?>? {
        val list: List<String?> = ArrayList()
        try {
            if (!dbData.isNullOrEmpty()) {
                return gson.fromJson(dbData, object : TypeToken<List<String?>?>() {}.type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    companion object {
        private val gson = Gson()
    }
}