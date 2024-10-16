package com.selbiconsulting.elog.ui.util

import ViolationType
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.load
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.enums.MessageStatus
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@BindingAdapter("setText")
fun setText(
    textView: TextView, text: String? = null
) {
    if (text.isNullOrEmpty()) {
        textView.visibility = View.GONE
        return
    }
    textView.text = text
}

@BindingAdapter("setImage")
fun setImage(
    imageView: ImageView, url: String? = null
) {
    imageView.load(url) {
        placeholder(R.color.primary_brand)
    }
}

@BindingAdapter("setStatus")
fun setMessageStatus(
    imageView: ImageView, status: String
) {
    val stateIcon = when (status) {
        MessageStatus.SENDING.value -> R.drawable.ic_waiting_message
        MessageStatus.SENT.value -> R.drawable.ic_checkmark
        MessageStatus.READ.value -> R.drawable.ic_read_message
        else -> R.drawable.ic_error_message
    }

    imageView.setImageResource(stateIcon)
}

@BindingAdapter("setFileSize")
fun setFileSize(textView: TextView, size: Long) {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var fileSize = size.toDouble()
    var unitIndex = 0
    while (fileSize > 1024 && unitIndex < units.size - 1) {
        fileSize /= 1024
        unitIndex++
    }

    textView.text = String.format("%.2f %s", fileSize, units[unitIndex])
}

@BindingAdapter("setTime")
fun setTime(textView: TextView, time: String?) {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    try {
        val parsedDate = inputFormat.parse(time)
        val timeZoneOffsetMillis = TimeZone.getDefault().rawOffset
        val formattedTime = parsedDate?.let {
            val adjustedTime = Date(it.time + timeZoneOffsetMillis)
            outputFormat.format(adjustedTime)
        }

        textView.text = formattedTime
    } catch (e: ParseException) {
        e.printStackTrace()
    }
}

@BindingAdapter("setTimeWithTimeZone")
fun setTimeWithTimeZone(textView: TextView, time: String?) {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Specify the input is in UTC
    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault() // Output in the device's default time zone

    try {
        val parsedDate = inputFormat.parse(time)
        val formattedTime = parsedDate?.let {
            outputFormat.format(it)
        }
        textView.text = formattedTime
    } catch (e: ParseException) {
        e.printStackTrace()
        textView.text = "" // Set text to empty if there's an error
    }
}

@BindingAdapter("setConvertedDate")
fun setConvertedDate(textView: TextView, time: String) {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    try {
        val parsedDate = inputFormat.parse(time)
        val timeZoneOffsetMillis = TimeZone.getDefault().rawOffset
        val formattedTime = parsedDate?.let {
            val adjustedTime = Date(it.time + timeZoneOffsetMillis)
            outputFormat.format(adjustedTime)
        }
        textView.text = formattedTime
    } catch (e: ParseException) {
        e.printStackTrace()
    }
}

@BindingAdapter("setViolations")
fun setViolations(textView: TextView, violations: String) {
    if (violations.isEmpty()) {
        textView.visibility = View.GONE
        return
    }

    textView.visibility = View.VISIBLE

    val violationList = violations.split(",").map { it.trim() }
    val formattedViolations = violationList.mapNotNull { violation ->
        when (violation) {
            ViolationType.PTI.serverName -> ViolationType.PTI.message
            ViolationType.DRIVING_WITHOUT_30_MINUTE_BREAK.serverName -> ViolationType.DRIVING_WITHOUT_30_MINUTE_BREAK.message
            ViolationType.ON_DUTY_WITHOUT_UNINTERRUPTED_BREAK.serverName -> ViolationType.ON_DUTY_WITHOUT_UNINTERRUPTED_BREAK.message
            ViolationType.DRIVING_EXCEEDS_11_HOURS.serverName -> ViolationType.DRIVING_EXCEEDS_11_HOURS.message
            ViolationType.EXCEEDED_70_HOUR_SHIFT.serverName -> ViolationType.EXCEEDED_70_HOUR_SHIFT.message
            else -> null
        }
    }

    val formattedText = formattedViolations.joinToString("\n") { "* $it" }
    textView.text = formattedText
}


