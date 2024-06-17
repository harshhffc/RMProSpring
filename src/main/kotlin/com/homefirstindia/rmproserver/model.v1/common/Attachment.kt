package com.homefirstindia.rmproserver.model.v1.common

import com.homefirstindia.rmproserver.utils.DateTimeUtils
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*
import kotlin.jvm.Transient

@Entity
@Table(name = "`Attachment`")
class Attachment {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    var id: String? = null

    @Column(nullable = false)
    var objectId: String? = null

    @Column(nullable = false)
    var objectType: String? = null

    @Column(nullable = false)
    var fileName: String? = null
    var contentType: String? = null

    @Column(nullable = false)
    var attachmentType: String? = null

    var fileIdentifier: String? = null

    @Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
    var createDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    @Column(columnDefinition = "DATETIME")
    var updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()

    @Transient
    var publicUrl: String? = null

    fun updateObjectDetails(objectId: String, objectType: String) {
        this.objectId = objectId
        this.objectType = objectType
    }
}