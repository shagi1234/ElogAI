package com.selbiconsulting.elog.ui.main.messages

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.entity.EntityMessage
import com.selbiconsulting.elog.databinding.ItemAlertMessageBinding
import com.selbiconsulting.elog.databinding.ItemChatDateBinding
import com.selbiconsulting.elog.databinding.ItemReceivedFileBinding
import com.selbiconsulting.elog.databinding.ItemReceivedTextMessageBinding
import com.selbiconsulting.elog.databinding.ItemReceivedVoiceBinding
import com.selbiconsulting.elog.databinding.ItemSentFileBinding
import com.selbiconsulting.elog.databinding.ItemSentImageBinding
import com.selbiconsulting.elog.databinding.ItemSentTextMessageBinding
import com.selbiconsulting.elog.databinding.ItemSentVoiceBinding
import com.selbiconsulting.elog.ui.util.AudioVisualizerView
import com.selbiconsulting.elog.ui.util.FileManager
import com.selbiconsulting.elog.ui.util.HelperFunctions
import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.sinh

sealed class HolderMessages(b: ViewBinding) : RecyclerView.ViewHolder(b.root) {
    class HolderReceivedText(private val b: ItemReceivedTextMessageBinding) : HolderMessages(b) {
        fun bind(message: EntityMessage) {
            b.message = message
            b.executePendingBindings()
        }
    }

    class HolderSentText(private val b: ItemSentTextMessageBinding) : HolderMessages(b) {
        fun bind(message: EntityMessage) {
            b.message = message
            b.executePendingBindings()
        }
    }

    class HolderReceivedFile(private val b: ItemReceivedFileBinding) : HolderMessages(b) {
        fun bind(message: EntityMessage) {
            b.message = message
            b.executePendingBindings()
        }
    }

    class HolderSentFile(private val b: ItemSentFileBinding) : HolderMessages(b) {
        fun bind(message: EntityMessage) {
            b.message = message
            b.executePendingBindings()
        }
    }

    class HolderChatDate(private val b: ItemChatDateBinding) : HolderMessages(b) {
        fun bind(context: Context, date: String) {

            b.tvDate.text = formatDate(date)
        }
        private fun formatDate(dateString: String): String {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
            val date = LocalDate.parse(dateString, inputFormatter)
            return date.format(outputFormatter)
        }
    }

    class HolderAlertMessage(
        private val b: ItemAlertMessageBinding,
    ) : HolderMessages(b) {
        private var player: ExoPlayer? = null
        fun bind(
            context: Context, message: EntityMessage,
            listener: AlertMessageListener,
            exoPlayer: ExoPlayer?,
            onAlertStartPlaying: (position: Int, mediaItem: MediaItem) -> Unit
        ) {
            b.message = message
            b.executePendingBindings()
            player = exoPlayer

//            if (message.isPlayedAlarm == false) {
//
//                val alarmId =
//                    if (message.fileSize.toInt() == 1) R.raw.battleship_alarm else if (message.fileSize.toInt() == 2) R.raw.alert_siren else R.raw.wave_alarm
//                val audioUri = Uri.parse("android.resource://${context.packageName}/${alarmId}")
//                val mediaItem = MediaItem.fromUri(audioUri)
//
//                onAlertStartPlaying(adapterPosition, mediaItem)
//
//                exoPlayer?.addListener(object : Player.Listener {
//                    override fun onPlaybackStateChanged(playbackState: Int) {
//                        if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE)
//                            listener.onAlertCompleted(message)
//                    }
//                })
//
//            }
        }
    }

    class HolderSentImage(private val b: ItemSentImageBinding) : HolderMessages(b) {
        fun bind(message: EntityMessage) {
            b.message = message
            b.executePendingBindings()
        }
    }

    class HolderSentVoice(private val b: ItemSentVoiceBinding) : HolderMessages(b) {
        private var isPlaying = false
        private var currentVoicePosition: Long = 0
        private lateinit var visualizerView: AudioVisualizerView
        fun bind(
            message: EntityMessage,
            context: Context,
            exoPlayer: ExoPlayer?,
            onVoiceItemClicked: (position: Int, mediaItem: MediaItem) -> Unit
        ) = with(b) {
            b.message = message
            b.executePendingBindings()

            val fileManager = FileManager()
            val audioFile = File(message.content)
            val audioFileLength = fileManager.getAudioFileLength(audioFile = audioFile)
            val amps = fileManager.getNormalizedAmps(audioFile = audioFile)

            visualizerView = AudioVisualizerView(
                context = context,
                amps = amps,
                parentLayout = visualizerLay,
                backgroundColor = R.color.brand200,
                progressColor = R.color.white_only,
                progressDuration = audioFileLength + 300L
            )
            visualizerView.create()

            clearAll()

            clickPlay.setOnClickListener {
                val mediaItem = MediaItem.fromUri(message.content.toUri())
                if (isPlaying) {
                    pauseVoice(visualizerView = visualizerView, exoPlayer = exoPlayer)
                } else {
                    playVoice(
                        uri = message.content.toUri(),
                        exoPlayer = exoPlayer,
                        visualizerView = visualizerView,
                    )
                    onVoiceItemClicked(adapterPosition, mediaItem)
                }
            }

            exoPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                        clearAll()
                    }
                    if (playbackState == Player.STATE_READY && exoPlayer.currentMediaItem == MediaItem.fromUri(
                            message.content.toUri()
                        )
                    ) {
                        isPlaying = true
                        updatePlayButtonIcon()
                        visualizerView.startProgress()
                    }

                }
            })


        }

        private fun clearAll() {
            b.icPlay.setImageResource(R.drawable.ic_play)
            visualizerView.clearProgress()
            isPlaying = false
            currentVoicePosition = 0

        }


        private fun playVoice(
            uri: Uri,
            exoPlayer: ExoPlayer?,
            visualizerView: AudioVisualizerView,
        ) {
            exoPlayer?.apply {
                val mediaItem = MediaItem.fromUri(uri)
                setMediaItem(mediaItem)
                prepare()
                if (currentVoicePosition > 0)
                    seekTo(currentVoicePosition)
                play()
            }


            isPlaying = true
            updatePlayButtonIcon()
            visualizerView.startProgress()
        }

        private fun pauseVoice(visualizerView: AudioVisualizerView, exoPlayer: ExoPlayer?) {
            exoPlayer?.pause()
            isPlaying = false
            currentVoicePosition = exoPlayer?.currentPosition ?: 0
            updatePlayButtonIcon()
            visualizerView.pausePlaying()
        }

        private fun updatePlayButtonIcon() {
            b.icPlay.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        }
    }

    class HolderReceivedVoice(private val b: ItemReceivedVoiceBinding) : HolderMessages(b) {
        private lateinit var visualizerView: AudioVisualizerView
        private var isPlaying = false
        private var currentVoicePosition: Long = 0
        fun bind(
            message: EntityMessage,
            context: Context,
            exoPlayer: ExoPlayer?,
            listener: ReceivedVoiceListener,
            onVoiceItemClicked: (position: Int, mediaItem: MediaItem) -> Unit
        ) = with(b) {
            b.message = message
            b.executePendingBindings()
            if (!File(message.content).isFile) {
                listener.onVoiceReceived(message)
                return@with
            }

            val fileManager = FileManager()
            val audioFile = File(message.content)
            val audioFileLength = message.fileSize * 1000
            val amps = fileManager.getNormalizedAmps(audioFile = audioFile)

            visualizerView = AudioVisualizerView(
                context = context,
                amps = amps,
                parentLayout = visualizerLay,
                backgroundColor = R.color.primary_disabled,
                progressColor = R.color.primary_brand,
                progressDuration = audioFileLength
            )
            visualizerView.create()

            clearAll()

            clickPlay.setOnClickListener {
                val mediaItem = MediaItem.fromUri(message.content.toUri())
                if (isPlaying) {
                    pauseVoice(visualizerView, exoPlayer)
                } else {
                    playVoice(
                        uri = message.content.toUri(),
                        exoPlayer = exoPlayer,
                        visualizerView = visualizerView,
                    )
                    onVoiceItemClicked(adapterPosition, mediaItem)
                }
            }

            exoPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                        clearAll()
                    }
                    if (playbackState == Player.STATE_READY && exoPlayer.currentMediaItem == MediaItem.fromUri(
                            message.content.toUri()
                        )
                    ) {
                        isPlaying = true
                        updatePlayButtonIcon()
                        visualizerView.startProgress()
                    }

                }
            })


        }

        private fun clearAll() {
            b.icPlay.setImageResource(R.drawable.ic_play)
            visualizerView.clearProgress()
            isPlaying = false
            currentVoicePosition = 0

        }


        private fun playVoice(
            uri: Uri,
            exoPlayer: ExoPlayer?,
            visualizerView: AudioVisualizerView,
        ) {
            exoPlayer?.apply {
                val mediaItem = MediaItem.fromUri(uri)
                setMediaItem(mediaItem)
                prepare()
                if (currentVoicePosition > 0)
                    seekTo(currentVoicePosition)
                play()
            }


            isPlaying = true
            updatePlayButtonIcon()
            visualizerView.startProgress()
        }

        private fun pauseVoice(visualizerView: AudioVisualizerView, exoPlayer: ExoPlayer?) {
            exoPlayer?.pause()
            isPlaying = false
            currentVoicePosition = exoPlayer?.currentPosition ?: 0
            updatePlayButtonIcon()
            visualizerView.pausePlaying()
        }

        private fun updatePlayButtonIcon() {
            b.icPlay.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        }

    }
}

