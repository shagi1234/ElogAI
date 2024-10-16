package com.selbiconsulting.elog.ui.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.view.View
import com.selbiconsulting.elog.ui.extentions.divide
import com.selbiconsulting.elog.ui.splash.ActivitySplash
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class HelperFunctions {

    companion object {
        var shared = HelperFunctions()
    }

    fun convertDateString(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val localDate = LocalDate.parse(dateString, inputFormatter)
        val localDateTime = localDate.atStartOfDay()
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        return localDateTime.atOffset(ZoneOffset.UTC).format(outputFormatter)
    }

    fun formattedDateString(outputPattern: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputFormatter = DateTimeFormatter.ofPattern(outputPattern)
        val dateTime = LocalDateTime.parse(getCurrentDate(), inputFormatter)
        val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        return outputFormatter.format(zonedDateTime)
    }

    fun getCreatedAt(): String {
        val currentDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
        val formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Adjust the format as needed
        return currentDateTime.format(formatter)
    }

    fun getCurrentDate(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Adjust the format as needed
        return currentDateTime.format(formatter)
    }


    fun createPdfFromView(context: Context, view: View): File? {
        // Create a PdfDocument with a page of the same size as the View
//        val pdfDocument = PdfDocument()
        var width = view.width
        var height = view.height

        if (width == 0 || height == 0) {
            val (measuredWidth, measuredHeight) = measureView(view)
            width = measuredWidth
            height = measuredHeight
        }

        // Ensure we have positive dimensions
        if (width <= 0) width = 1
        if (height <= 0) height = 1

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        // Draw the view content to the PDF page
        val canvas = page.canvas
        view.draw(canvas)

        // Finish the page and save the document
        pdfDocument.finishPage(page)

        // Create file
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        // Create file
        val filePath = File(context.cacheDir, "com.selbiconsulting_${timeStamp}_.pdf")

        try {
            // Write the PDF file to storage
            pdfDocument.writeTo(FileOutputStream(filePath))
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            // Close the document
            pdfDocument.close()
        }

        return if (filePath.exists()) filePath else null
    }

    private fun measureView(view: View): Pair<Int, Int> {
        val specWidth = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

        view.measure(specWidth, specHeight)

        return Pair(view.measuredWidth, view.measuredHeight)
    }


    fun isLessThan30Minutes(timeString: String): Boolean {
        val (hours, minutes) = timeString.split(":").map { it.toInt() }

        val totalMinutes = hours * 60 + minutes

        return (totalMinutes in 1..Const.lessThan30Mins.divide(Const.divider))
    }

    fun isProgressEnded(timeString: String): Boolean {
        val (hours, minutes) = timeString.split(":").map { it.toInt() }

        val totalMinutes = hours * 60 + minutes

        return totalMinutes == 0
    }

    fun calculateProgressInMinutes(time: String): Int {
        val parts = time.split(":")
        val hours = parts[0].toLongOrNull() ?: 0
        val minutes = parts[1].toLongOrNull() ?: 0
        return (hours * 60 + minutes).toInt()
    }

    fun convertToMinutes(timeString: String): Int {
        val (hours, minutes) = timeString.split(":").map { it.toInt() }
        return hours * 60 + minutes
    }
//    fun createPdfFromView(context: Context, view: View): File? {
//        val scrollView = view.findViewById<NestedScrollView>(R.id.main_view)
//        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_daily_log)
//
//        // Measure and layout the entire view
//        view.measure(
//            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
//            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//        )
//        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
//
//        // Calculate the full height of the view including all RecyclerView items
//        val recyclerViewAdapter = recyclerView.adapter
//        var totalHeight = scrollView.measuredHeight
//
//        if (recyclerViewAdapter != null) {
//            for (i in 0 until recyclerViewAdapter.itemCount) {
//                val holder = recyclerViewAdapter.createViewHolder(recyclerView, recyclerViewAdapter.getItemViewType(i))
//                recyclerViewAdapter.onBindViewHolder(holder, i)
//                holder.itemView.measure(
//                    View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
//                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//                )
//                totalHeight += holder.itemView.measuredHeight
//            }
//        }
//
//        // Create a bitmap to draw the full view
//        val bitmap = Bitmap.createBitmap(view.measuredWidth, totalHeight, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//
//        // Draw the ScrollView content
//        scrollView.draw(canvas)
//
//        // Draw all RecyclerView items
//        var itemTop = scrollView.measuredHeight
//        if (recyclerViewAdapter != null) {
//            for (i in 0 until recyclerViewAdapter.itemCount) {
//                val holder = recyclerViewAdapter.createViewHolder(recyclerView, recyclerViewAdapter.getItemViewType(i))
//                recyclerViewAdapter.onBindViewHolder(holder, i)
//                holder.itemView.measure(
//                    View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
//                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//                )
//                holder.itemView.layout(0, 0, holder.itemView.measuredWidth, holder.itemView.measuredHeight)
//                canvas.save()
//                canvas.translate(recyclerView.left.toFloat(), itemTop.toFloat())
//                holder.itemView.draw(canvas)
//                canvas.restore()
//                itemTop += holder.itemView.measuredHeight
//            }
//        }
//
//        // Create PDF from the bitmap
//        val pdfDocument = PdfDocument()
//        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
//        val page = pdfDocument.startPage(pageInfo)
//
//        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
//
//        pdfDocument.finishPage(page)
//
//        // Create file
//        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//        val filePath = File(context.cacheDir, "com.selbiconsulting_${timeStamp}_.pdf")
//
//        try {
//            pdfDocument.writeTo(FileOutputStream(filePath))
//        } catch (e: IOException) {
//            e.printStackTrace()
//            return null
//        } finally {
//            pdfDocument.close()
//            bitmap.recycle()
//        }
//
//        return if (filePath.exists()) filePath else null
//    }

}


fun Activity.logout() {
    val loginIntent = Intent(this, ActivitySplash::class.java)
//        viewModelChangeStatus.clearLocalDb()
    startActivity(loginIntent)
    this.finish()
}


fun String.toDateMillis(): Long {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    return LocalDateTime.parse(this, formatter)
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()
}


