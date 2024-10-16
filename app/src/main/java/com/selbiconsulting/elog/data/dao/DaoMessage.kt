package com.selbiconsulting.elog.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.selbiconsulting.elog.data.model.entity.EntityMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoMessage : DaoBase<EntityMessage> {
    @Query("SELECT * FROM message")
    fun getAllMessages(): Flow<List<EntityMessage>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(message: EntityMessage)


    @Query("SELECT COUNT(*) FROM message WHERE isSenderMe = 0 AND isRead = 0")
    suspend fun getUnreadMessageCount(): Int

    @Query("UPDATE message SET isRead = 1 WHERE isSenderMe = 0")
    suspend fun markAllMessagesAsRead()

    @Transaction
    suspend fun upsert(messages: List<EntityMessage>) {
        for (message in messages) {
            val existingMessage = getMessageByRemoteId(message.id ?: "")

            if (existingMessage != null) {
                message.localId = existingMessage.localId
            }

            insertOrUpdate(message)
        }
    }

    @Query("SELECT * FROM message WHERE id = :id LIMIT 1")
    suspend fun getMessageByRemoteId(id: String): EntityMessage?

    @Query(
        """
        UPDATE message 
        SET status = :readStatus 
        WHERE createdDate < :it
    """
    )
    suspend fun updateMessagesStatus(it: String, readStatus: String = "read")

    @Query(
        """
        UPDATE message 
        SET content = :content 
        WHERE id = :id
    """
    )
    suspend fun updateMessageContentById(id: String, content: String)


}