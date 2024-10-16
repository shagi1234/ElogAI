package com.selbiconsulting.elog.data.model.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(tableName = "drivers")
data class EntityDriver(
    @PrimaryKey(autoGenerate = true)
    var localId: Long? = null,
    var statusCode: String?,
    var vin: String?,
    var trailerId: String?,
    var vehicleId: String?,
    var driverId: String?,
    var mobilePhone: String?,
    var phone: String?,
    var email: String?,
    var license: String?,
    var carrier: String?,
    var mainTerminal: String?,
    var co_driver: String?,
    var username: String?,
    var lastname: String?,
    var firstname: String?,
    var name: String?,
    var document: String?,
    var note: String?,
    var id: String,
) : Parcelable