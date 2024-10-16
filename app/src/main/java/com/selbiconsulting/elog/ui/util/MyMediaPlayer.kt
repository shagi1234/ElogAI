package com.selbiconsulting.elog.ui.util

import android.media.MediaPlayer

class MyMediaPlayer {
    companion object{
        var shared = MyMediaPlayer()
    }

    var mediaPlayer: MediaPlayer? = null
}