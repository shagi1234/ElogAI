package com.selbiconsulting.elog.ui.util.audio_player

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

import java.io.File
import javax.inject.Inject


class AudioPlayerImpl @Inject constructor(
    private val exoPlayer: ExoPlayer
) : AudioPlayer {

    private var currentPos: Long = 0
    override fun getInstance(): ExoPlayer {
        return exoPlayer
    }

    override fun playFile(file: File) {
        exoPlayer.stop()
        exoPlayer.also { exoPlayer ->
            // create a media item.
            val mediaItem = MediaItem.fromUri(file.toUri())
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (currentPos > 0)
                exoPlayer.seekTo(currentPos)
            exoPlayer.play()
        }
    }

    override fun stopPlaying() {
        try {
            exoPlayer.stop()
            currentPos = 0
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    override fun pause() {
        currentPos = exoPlayer.currentPosition ?: 0
        exoPlayer.pause()
    }

    override fun resume() {
        exoPlayer.seekTo(currentPos)

    }

    override fun onCompletion(onCompletion: () -> Unit) {

    }

    override fun isPlaying(): Boolean {
        return exoPlayer.isPlaying ?: false
    }
}


