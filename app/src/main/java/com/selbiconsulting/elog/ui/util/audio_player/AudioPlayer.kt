package com.selbiconsulting.elog.ui.util.audio_player

import androidx.media3.exoplayer.ExoPlayer
import java.io.File

interface AudioPlayer {
    fun getInstance(): ExoPlayer
    fun playFile(file: File)
    fun stopPlaying()
    fun pause()
    fun resume()
    fun onCompletion(onCompletion: () -> Unit)
    fun isPlaying():Boolean
}