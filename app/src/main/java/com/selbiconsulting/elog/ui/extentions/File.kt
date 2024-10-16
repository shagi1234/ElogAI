package com.selbiconsulting.elog.ui.extentions

import java.io.File
import java.io.OutputStream
import java.util.Base64

fun File.toBase64(): String {
    val bytes = this.readBytes()
    return Base64.getEncoder().encodeToString(bytes)
}

fun File.copyTo(outputStream: OutputStream): Long {
    return inputStream().use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
}

fun File.copyTo(destination: File) {
    inputStream().use { input ->
        destination.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}