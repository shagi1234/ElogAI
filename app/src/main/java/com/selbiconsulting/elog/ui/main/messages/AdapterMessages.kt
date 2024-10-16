package com.selbiconsulting.elog.ui.main.messages

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.selbiconsulting.elog.data.model.entity.ChatItem
import com.selbiconsulting.elog.data.model.entity.EntityMessage
import com.selbiconsulting.elog.data.model.enums.MessageType
import com.selbiconsulting.elog.data.model.enums.MessagesType
import com.selbiconsulting.elog.databinding.ItemAlertMessageBinding
import com.selbiconsulting.elog.databinding.ItemChatDateBinding
import com.selbiconsulting.elog.databinding.ItemReceivedFileBinding
import com.selbiconsulting.elog.databinding.ItemReceivedTextMessageBinding
import com.selbiconsulting.elog.databinding.ItemReceivedVoiceBinding
import com.selbiconsulting.elog.databinding.ItemSentFileBinding
import com.selbiconsulting.elog.databinding.ItemSentImageBinding
import com.selbiconsulting.elog.databinding.ItemSentTextMessageBinding
import com.selbiconsulting.elog.databinding.ItemSentVoiceBinding
import com.selbiconsulting.elog.ui.util.DiffUtilMessages

class AdapterMessages(
    private val context: Context,
    private val receivedVoiceListener: ReceivedVoiceListener,
    private var exoPlayer: ExoPlayer? = ExoPlayer.Builder(context).build(),
    private var alertMessageListener: AlertMessageListener

) : RecyclerView.Adapter<HolderMessages>() {

    private var messages: MutableList<EntityMessage> = mutableListOf()
    private var lastPlayedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderMessages {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            MessagesType.RECEIVED_TEXT.ordinal -> HolderMessages.HolderReceivedText(
                ItemReceivedTextMessageBinding.inflate(inflater, parent, false)
            )

            MessagesType.CHAT_DATE.ordinal -> HolderMessages.HolderChatDate(
                ItemChatDateBinding.inflate(inflater, parent, false)
            )

            MessagesType.SENT_TEXT.ordinal -> HolderMessages.HolderSentText(
                ItemSentTextMessageBinding.inflate(inflater, parent, false)
            )

            MessagesType.RECEIVED_FILE.ordinal -> HolderMessages.HolderReceivedFile(
                ItemReceivedFileBinding.inflate(inflater, parent, false)
            )

            MessagesType.SENT_FILE.ordinal -> HolderMessages.HolderSentFile(
                ItemSentFileBinding.inflate(inflater, parent, false)
            )

            MessagesType.SENT_IMAGE.ordinal -> HolderMessages.HolderSentImage(
                ItemSentImageBinding.inflate(inflater, parent, false)
            )

            MessagesType.SENT_VOICE.ordinal -> HolderMessages.HolderSentVoice(
                ItemSentVoiceBinding.inflate(inflater, parent, false)
            )

            MessagesType.RECEIVED_VOICE.ordinal -> HolderMessages.HolderReceivedVoice(
                ItemReceivedVoiceBinding.inflate(inflater, parent, false)
            )

            else -> HolderMessages.HolderAlertMessage(
                ItemAlertMessageBinding.inflate(inflater, parent, false)
            )


        }
    }


    override fun onBindViewHolder(holder: HolderMessages, position: Int) {
        val message = messages[position]
        when (holder) {
            is HolderMessages.HolderAlertMessage -> holder.bind(
                context = context,
                exoPlayer = exoPlayer,
                message = message,
                listener = alertMessageListener
            ) { pos, mediaItem ->
                onAlertStartPlaying(pos, mediaItem)
            }

            is HolderMessages.HolderChatDate -> holder.bind(
                context = context,
                date = message.createdDate?:""
            )

            is HolderMessages.HolderReceivedFile -> holder.bind(message = message)
            is HolderMessages.HolderReceivedText -> holder.bind(message = message)
            is HolderMessages.HolderSentFile -> holder.bind(message = message)
            is HolderMessages.HolderSentImage -> holder.bind(message = message)
            is HolderMessages.HolderSentText -> holder.bind(message = message)
            is HolderMessages.HolderSentVoice -> holder.bind(
                message = message,
                context = context,
                exoPlayer = exoPlayer
            ) { pos, mediaItem ->
                handleVoiceItemClicked(pos, mediaItem)
            }

            is HolderMessages.HolderReceivedVoice -> holder.bind(
                message = message,
                context = context,
                exoPlayer = exoPlayer,
                listener = receivedVoiceListener,
            )
            { pos, mediaItem ->
                handleVoiceItemClicked(pos, mediaItem)
            }
        }
    }

    override fun getItemCount() = messages.size


    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when (message.messageType) {
                MessageType.TEXT.value -> {
                    if (message.isSenderMe) MessagesType.SENT_TEXT.ordinal
                    else MessagesType.RECEIVED_TEXT.ordinal
                }

                MessageType.IMAGE.value -> {
                    if (message.isSenderMe) MessagesType.SENT_IMAGE.ordinal
                    else MessagesType.RECEIVED_IMAGE.ordinal
                }

                MessageType.FILE.value -> {
                    if (message.isSenderMe) MessagesType.SENT_TEXT.ordinal
                    else MessagesType.RECEIVED_TEXT.ordinal
                }

                MessageType.VOICE.value -> {
                    if (message.isSenderMe) MessagesType.SENT_VOICE.ordinal
                    else MessagesType.RECEIVED_VOICE.ordinal
                }

                else -> { // ALARM
                    MessagesType.ALERT.ordinal
                }
            }
        }

    fun updateData(newList: List<EntityMessage>) {
        val diffCallback = DiffUtilMessages(messages, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messages.clear()
        messages.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }


    private fun processMessagesWithDateSeparators(messages: List<EntityMessage>): List<ChatItem> {
        val result = mutableListOf<ChatItem>()
        var lastDate: String? = null

        messages.forEach { message ->
            val messageDate = message.createdDate?.substring(0, 10) // Assuming "yyyy-MM-dd" format
            if (messageDate != lastDate) {
                result.add(ChatItem(message, true, messageDate ?: ""))
                lastDate = messageDate
            }
            result.add(ChatItem(message))
        }

        return result
    }

    private fun handleVoiceItemClicked(position: Int, mediaItem: MediaItem) {
        if (!wasPlayedBefore(position)) {
            releaseLastPlayedExoPlayer()
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                setMediaItem(mediaItem)
                prepare()
                play()
            }
            notifyItemChanged(position, Unit)
            notifyItemChanged(lastPlayedPosition, Unit)
        }
        lastPlayedPosition = position
    }

    private fun onAlertStartPlaying(position: Int, mediaItem: MediaItem) {
        if (!wasPlayedBefore(position)) {
            releaseLastPlayedExoPlayer()
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                setMediaItem(mediaItem)
                prepare()
                play()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                notifyItemChanged(position, Unit)
                notifyItemChanged(lastPlayedPosition, Unit)

            }, 200)
        }
        lastPlayedPosition = position

    }

    private fun wasPlayedBefore(position: Int) = (lastPlayedPosition == position)

    private fun releaseLastPlayedExoPlayer() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }


}

interface ReceivedVoiceListener {
    fun onVoiceReceived(message: EntityMessage)
}

interface AlertMessageListener {
    fun onAlertCompleted(message: EntityMessage)
}

