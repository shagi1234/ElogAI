package com.selbiconsulting.elog.ui.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.selbiconsulting.elog.ui.extentions.copyTo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.abs

class FileManager {
    companion object {
        var shared = FileManager()
    }

    fun readFileFromInternalStorage(context: Context, fileName: String): ByteArray? {
        return try {
            val inputStream = context.openFileInput(fileName)
            val bytes = inputStream.readBytes()
            inputStream.close()
            bytes
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveFileToInternalStorage(context: Context, file: File, fileName: String): Boolean {
        return try {
            val outputStream: FileOutputStream =
                context.openFileOutput(fileName, Context.MODE_PRIVATE)
            file.copyTo(outputStream)
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun createFileFromByteArray(byteArray: ByteArray, filePath: String): Boolean {
        return try {
            val outputStream = FileOutputStream(filePath)
            outputStream.write(byteArray)
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            filePath = it.getString(columnIndex)
        }
        return filePath
    }

    fun getNormalizedAmps(audioFile: File):List<Float>{
        return getEquidistantSublist(loadAudioAmps(audioFile)
        )

    }
  private  fun loadAudioAmps(audioFile: File): List<Float> {
        val amplitudes = mutableListOf<Float>()
        try {
            FileInputStream(audioFile).use { inputStream ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val shorts = ByteBuffer.wrap(buffer).asShortBuffer()
                    val chunkAmplitudes = mutableListOf<Float>()
                    for (i in 0 until bytesRead / 2) {
                        val amplitude = abs(shorts.get(i).toFloat())
                        chunkAmplitudes.add(amplitude)
                    }
                    amplitudes.addAll(chunkAmplitudes)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return amplitudes
    }

    private fun getEquidistantSublist(
        inputList: List<Float>,
        desiredSize: Int = 26
    ): List<Float> {
        if (inputList.isEmpty())
            return emptyList()
        if (inputList.size <= desiredSize) return inputList

        val stepSize = inputList.size / desiredSize

        val equidistantList = mutableListOf<Float>()

        for (i in 0 until desiredSize) {
            val index = i * stepSize
            equidistantList.add(inputList.getOrElse(index) { inputList.last() })
        }

        return equidistantList
    }


    fun getAudioFileLength(audioFile: File): Long {
        Log.e("AUDIO_FILE", "getAudioFileLength: ", )

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(audioFile.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLong() ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        } finally {
            retriever.release()
        }
//        val mediaPlayer = MediaPlayer()
//        return try {
//            mediaPlayer.setDataSource(audioFile.absolutePath)
//            mediaPlayer.prepare()
//            // Duration in milliseconds
//            mediaPlayer.duration
//        } catch (e: IOException) {
//            e.printStackTrace()
//            1
//        } finally {
//            mediaPlayer.release()
//        }
    }

}