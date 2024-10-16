package com.selbiconsulting.elog.ui.main.messages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.common.ResponseMessage
import com.selbiconsulting.elog.data.model.dto.GetMessage
import com.selbiconsulting.elog.data.model.entity.EntityMessage
import com.selbiconsulting.elog.data.model.enums.MessageStatus
import com.selbiconsulting.elog.data.model.enums.MessageType
import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.request.RequestSendMessage
import com.selbiconsulting.elog.data.model.response.ResponseGetMessage
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.repository.RepositoryMessage
import com.selbiconsulting.elog.domain.use_case.UseCAseGetFileUrl
import com.selbiconsulting.elog.domain.use_case.UseCaseClearLocalDb
import com.selbiconsulting.elog.domain.use_case.UseCaseGetMessage
import com.selbiconsulting.elog.domain.use_case.UseCaseLogin
import com.selbiconsulting.elog.domain.use_case.UseCaseSendMessage
import com.selbiconsulting.elog.ui.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ViewModelMessage @Inject constructor(
    private var repositoryMessage: RepositoryMessage,
    private var useCaseGetMessage: UseCaseGetMessage,
    private var useSendMessage: UseCaseSendMessage,
    private val useCaseGetFileUrl: UseCAseGetFileUrl,
    private val useCaseClearLocalDb: UseCaseClearLocalDb,
    private val useCaseLogin: UseCaseLogin,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
) : ViewModel() {

    val messages: MutableStateFlow<List<EntityMessage>> = MutableStateFlow(emptyList())

    private val _messagesState: MutableStateFlow<Resource<ResponseGetMessage>> = MutableStateFlow(
        Resource.Success()
    )
    val messagesState get() = _messagesState

    private val _unreadMessageCount = MutableLiveData<Int>()
    val unreadMessageCount: LiveData<Int> = _unreadMessageCount

    private val _inChat = MutableLiveData<Int>()
    val inChat get() = _inChat


    val lastReadDate: MutableStateFlow<String> = MutableStateFlow("")

    private val _fileUrlState = SingleLiveEvent<Resource<ByteArray>>()
    val fileUrlState: SingleLiveEvent<Resource<ByteArray>> get() = _fileUrlState

    private val _currentMessage = SingleLiveEvent<EntityMessage>()
    val currentMessage get() = _currentMessage


    init {
        viewModelScope.launch {
            messages.value = getAllMessagesLocal().first()
        }
    }


    fun setInChat(inChat: Int) {
        _inChat.value = inChat
    }

    fun getFileUrl(requestGetMessage: RequestGetMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            _fileUrlState.postValue(useCaseGetFileUrl.execute(requestGetMessage))
        }

    }


    private suspend fun getAllMessagesLocal(): Flow<List<EntityMessage>> {
        return repositoryMessage.getAllMessagesLocal()
    }

    private suspend fun insertMessage(message: EntityMessage): Long {
        return repositoryMessage.insertMessage(message)
    }

    fun insertCurrentMessage(message: EntityMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryMessage.insertOrUpdateMessage(message)
        }
        _currentMessage.value = message
    }


    fun handleMessagesSuccess(message: ResponseGetMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            message.lastReadDate?.let { lastReadDate.emit(it) }
            message.messages.let { messages ->
                upsertMessages(messages.toEntityMessages())
                _unreadMessageCount.postValue(calculateUnreadCount())
            }
        }
    }

    fun getMessagesFromServer(requestGetMessage: RequestGetMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            _messagesState.emit(useCaseGetMessage.execute(requestGetMessage))

//            when (val result = useCaseGetMessage.execute(requestGetMessage)) {
//                is Resource.Error -> {}
//                is Resource.Failure -> {}
//                is Resource.Loading -> {
//
//                }
//
//                is Resource.Success -> {
//                    result.data?.lastReadDate?.let { lastReadDate.emit(it) }
//                    result.data?.messages?.let { messages ->
//                        Log.e("SERVER_MESSAGE", "getMessagesFromServer: ${messages}")
//                        upsertMessages(messages.toEntityMessages())
//                        _unreadMessageCount.postValue(calculateUnreadCount())
//
//                    }
//                }
//            }
        }
    }

    fun markAllMessagesAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryMessage.markAllMessagesAsRead()
        }
    }

    private suspend fun calculateUnreadCount(): Int {
        return repositoryMessage.getUnreadMessageCount()
    }

    private fun getClassicWithTime(): String {
        val calendar =
            Calendar.getInstance(TimeZone.getTimeZone("UTC")) // Ensure using a fixed timezone
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        fmt.timeZone = TimeZone.getTimeZone("UTC") // Ensure using a fixed timezone
        return fmt.format(calendar.time)
    }

    suspend fun sendMessageToServer(requestSendMessage: RequestSendMessage) {
        var localId = 0L

        viewModelScope.launch(Dispatchers.IO) {
            val entityMessage = EntityMessage(
                content = requestSendMessage.message,
                isSenderMe = true,
                messageType = requestSendMessage.messageType,
                status = MessageStatus.SENDING.value,
                fileSize = requestSendMessage.fileSize,
                createdDate = getClassicWithTime(),
                localPath = requestSendMessage.file?.filename,
                id = ""
            )

            localId = insertMessage(entityMessage)

            updateMessages()
        }


        viewModelScope.launch(Dispatchers.IO) {
            when (val result = useSendMessage.execute(requestSendMessage)) {
                is Resource.Error -> {}
                is Resource.Failure -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    val entityMessage = result.data?.let {
                        EntityMessage(
                            content = requestSendMessage.message,
                            isSenderMe = true,
                            messageType = requestSendMessage.messageType,
                            status = MessageStatus.SENT.value,
                            fileSize = requestSendMessage.fileSize,
                            createdDate = it.createdDate,
                            localPath = requestSendMessage.file?.filename,
                            id = it.id,
                            localId = localId
                        )
                    }

                    if (entityMessage != null) {
                        upsertMessage(entityMessage)
                    }
                }
            }
        }


    }

    private suspend fun upsertMessages(messages: List<EntityMessage>) {
        if (messages.isEmpty()) return

        val newMessages = messages.filter {
            repositoryMessage.getMessageByRemoteId(it.id.toString()) == null
        }

        if (newMessages.isNotEmpty()) {
            repositoryMessage.upsertMessages(newMessages)
            for (message in newMessages) {
                if (!message.isSenderMe) {
                    message.isRead = false  // Mark incoming messages as unread
                }
            }

            updateMessages()
        }
    }


    private suspend fun upsertMessage(message: EntityMessage) {
        repositoryMessage.upsertMessage(message)
        updateMessages()
    }

    private suspend fun updateMessages() {
        messages.value = getAllMessagesLocal().first()
    }

    suspend fun updateMessagesStatus(it: String) {
        repositoryMessage.updateMessagesStatus(it)
    }

    suspend fun updateMessageByRemoteId(id: String, content: String) {
        repositoryMessage.updateMessageContentById(id, content)
        updateMessages()
    }

    fun updateMessage(message: EntityMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryMessage.upsertMessage(message)
            updateMessages()
        }
    }

    fun clearLocalDb() {
        viewModelScope.launch(Dispatchers.IO) {
            useCaseClearLocalDb.execute()
        }
    }


    fun reLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            val requestLogin = RequestLogin(
                username = sharedPreferencesHelper.username,
                password = sharedPreferencesHelper.password,
                deviceId = sharedPreferencesHelper.deviceID,
                deviceToken = sharedPreferencesHelper.fcm_token
            )
            val result = useCaseLogin.execute(requestLogin)
            if (result is Resource.Success) {
                sharedPreferencesHelper.token = result.data?.accessToken
                val requestGetMessage = RequestGetMessage(
                    contactId = sharedPreferencesHelper.contactId.toString(),
                    inChat = _inChat.value
                )
                getMessagesFromServer(requestGetMessage = requestGetMessage)
            }
        }
    }
}

fun safeMessageStatus(value: String?): MessageStatus {
    return try {
        MessageStatus.valueOf(value?.uppercase(Locale.ROOT) ?: "SENT")
    } catch (e: IllegalArgumentException) {
        MessageStatus.SENT // Default to SENT if value is invalid
    }
}


fun List<GetMessage>.toEntityMessages(): List<EntityMessage> {
    return map { getMessage ->
        EntityMessage(
            localId = null,
            id = getMessage.id,
            content = getMessage.message,
            isSenderMe = false,
            messageType = getMessage.messageType,
            status = safeMessageStatus(getMessage.messageStatus).value,
            fileSize = getMessage.fileSize.toLong(),
            createdDate = getMessage.createdDate,
            localPath = null
        )
    }
}
