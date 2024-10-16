package com.selbiconsulting.elog.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "message")
data class EntityMessage(
    @PrimaryKey(autoGenerate = true)
    var localId: Long? = null,
    var id: String? = "",
    var content: String,
    var isSenderMe: Boolean,
    var messageType: String,
    var status: String,
    var fileSize: Long,
    var createdDate: String?,// "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    var localPath: String?,
    var isPlayedAlarm:Boolean?=false,
    var isRead: Boolean = false
)

data class ChatItem(
    val message: EntityMessage,
    val showDateSeparator: Boolean = false,
    val separatorDate: String = ""
)