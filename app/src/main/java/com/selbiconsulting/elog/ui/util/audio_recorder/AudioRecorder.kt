package com.selbiconsulting.elog.ui.util.audio_recorder

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
}