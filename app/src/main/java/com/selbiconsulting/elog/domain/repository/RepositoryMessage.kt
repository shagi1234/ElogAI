package com.selbiconsulting.elog.domain.repository

import com.selbiconsulting.elog.data.model.entity.EntityMessage
import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.data.model.request.RequestSendMessage
import com.selbiconsulting.elog.data.model.response.ResponseGetFileUrl
import com.selbiconsulting.elog.data.model.response.ResponseGetMessage
import com.selbiconsulting.elog.data.model.response.ResponseSendMessage
import kotlinx.coroutines.flow.Flow

interface RepositoryMessage {
    suspend fun getMessagesFromServer(requestGetMessage: RequestGetMessage): ResponseGetMessage
    suspend fun sendMessageToServer(requestSendMessage: RequestSendMessage): ResponseSendMessage
    suspend fun getAllMessagesLocal(): Flow<List<EntityMessage>>
    suspend fun getMessageByRemoteId(id: String): EntityMessage?
    suspend fun insertMessage(message: EntityMessage): Long
    suspend fun insertOrUpdateMessage(message: EntityMessage)
    suspend fun upsertMessages(messages: List<EntityMessage>)
    suspend fun upsertMessage(messages: EntityMessage)
    suspend fun updateMessagesStatus(it: String)
    suspend fun updateMessageContentById(id: String, content: String)
    suspend fun getFileUrl(requestGetMessage: RequestGetMessage): ByteArray
    suspend fun getUnreadMessageCount(): Int
    suspend fun markAllMessagesAsRead()


}