package com.selbiconsulting.elog.data.repository

import com.selbiconsulting.elog.data.model.entity.EntityMessage
import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.data.model.request.RequestSendMessage
import com.selbiconsulting.elog.data.model.response.ResponseGetMessage
import com.selbiconsulting.elog.data.model.response.ResponseSendMessage
import com.selbiconsulting.elog.data.storage.local.AppDatabase
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.data.storage.remote.service.ServiceMessage
import com.selbiconsulting.elog.domain.repository.RepositoryMessage
import io.ktor.utils.io.concurrent.shared
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepositoryMessageImpl @Inject constructor(
    private val serviceServiceMessage: ServiceMessage,
    private val appDatabase: AppDatabase,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : RepositoryMessage {
    override suspend fun getMessagesFromServer(requestGetMessage: RequestGetMessage): ResponseGetMessage {
        return serviceServiceMessage.getMessage(
            requestGetMessage,
            sharedPreferencesHelper.deviceID.toString()
        )
    }

    override suspend fun sendMessageToServer(requestSendMessage: RequestSendMessage): ResponseSendMessage {
        return serviceServiceMessage.sendMessage(requestSendMessage)
    }

    override suspend fun getAllMessagesLocal(): Flow<List<EntityMessage>> {
        return appDatabase.messageDao().getAllMessages()
    }

    override suspend fun getMessageByRemoteId(id: String): EntityMessage? {
        return appDatabase.messageDao().getMessageByRemoteId(id)
    }

    override suspend fun insertMessage(message: EntityMessage): Long {
        return appDatabase.messageDao().insert(message)
    }

    override suspend fun insertOrUpdateMessage(message: EntityMessage) {
        appDatabase.messageDao().insertOrUpdate(message)
    }

    override suspend fun upsertMessage(messages: EntityMessage) {
        appDatabase.messageDao().update(messages)
    }

    override suspend fun updateMessagesStatus(it: String) {
        appDatabase.messageDao().updateMessagesStatus(it)
    }

    override suspend fun updateMessageContentById(id: String, content: String) {
        appDatabase.messageDao().updateMessageContentById(id, content)
    }

    override suspend fun getFileUrl(requestGetMessage: RequestGetMessage): ByteArray {
        return serviceServiceMessage.getFileUrl(
            requestGetMessage,
            sharedPreferencesHelper.deviceID.toString()
        )
    }

    override suspend fun getUnreadMessageCount(): Int {
        return appDatabase.messageDao().getUnreadMessageCount()
    }

    override suspend fun markAllMessagesAsRead() {
        appDatabase.messageDao().markAllMessagesAsRead()
    }


    override suspend fun upsertMessages(messages: List<EntityMessage>) {
        appDatabase.messageDao().upsert(messages)
    }

}