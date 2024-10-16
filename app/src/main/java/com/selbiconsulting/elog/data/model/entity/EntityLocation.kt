package com.selbiconsulting.elog.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


/*
 * Created by shagi on 03.04.2024 14:44
 */

@Serializable
@Entity(tableName = "location")
data class EntityLocation(
    @PrimaryKey var locationId: Long,
    var longitude: String, // Renamed field from 'long' to 'longitude'
    var lat: String,
    var locationName: String,
    var speed: String,
    var engHours: String,
    var distance: String,
    var isSynchronized: Boolean,
    var createdAt: String
)