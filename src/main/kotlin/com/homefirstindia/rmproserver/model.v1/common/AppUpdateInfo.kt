package com.homefirstindia.rmproserver.model.v1.common

import com.homefirstindia.rmproserver.utils.DateTimeUtils
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "`AppUpdateInfo`")
class AppUpdateInfo {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    var title: String? = null
    var description: String? = null
    var androidVersionName: String? = null
    var iosVersionName: String? = null

    var isOptional = false

    @Lob
    @Convert(converter = FeaturesConverter::class)
    val features: ArrayList<String> = ArrayList()

    @ColumnDefault("0")
    var isActive = false

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime: String = DateTimeUtils.getCurrentDateTimeInIST()

}