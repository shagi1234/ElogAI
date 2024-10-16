package com.selbiconsulting.elog.ui.main.logs_page

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.model.enums.getShortDutyStatus
import com.selbiconsulting.elog.databinding.BottomSheetLogsDataBinding
import com.selbiconsulting.elog.ui.main.flow.FragmentFlowDirections
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog
import com.selbiconsulting.elog.ui.util.HelperFunctions
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class BottomSheetLogDetails(
    private val context: Context,
    private val currentData: DataLog
) : BottomSheetDialogFragment() {
    private lateinit var b: BottomSheetLogsDataBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = BottomSheetLogsDataBinding.inflate(LayoutInflater.from(context), container, false)
        setStatusColors(currentData)
        setLogInfo()
        return b.root
    }

    private fun setLogInfo() {
        b.tvStatus.text = getShortDutyStatus(currentData.statusShort)
        b.tvDocs.text = currentData.document
        b.tvTrailers.text = currentData.trailer
        b.tvNotes.text = currentData.note
        b.tvTime.text = convertDate(currentData.createdAt)
        b.tvDuration.text = getTimeDifference(currentData.endTime, currentData.timeStart)
        b.tvLocation.text = currentData.location
        b.tvOdometer.text = currentData.odometer
    }

    private fun convertDate(time:String?):String{
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

       return try {
            val parsedDate = inputFormat.parse(time)
            val timeZoneOffsetMillis = TimeZone.getDefault().rawOffset
            val formattedTime = parsedDate?.let {
                val adjustedTime = Date(it.time + timeZoneOffsetMillis)
                outputFormat.format(adjustedTime)
            }
           formattedTime?:""

        } catch (e: ParseException) {
            e.printStackTrace()
           ""
        }
    }
    private fun getTimeDifference(endTime: String?, timeStart: String?): String {
        Log.e("TIME__", "getTimeDifference: $timeStart \n $endTime" )
        if (endTime == null || timeStart == null) {
            return "Invalid input"
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val start = LocalDateTime.parse(timeStart, formatter)
        val end = LocalDateTime.parse(endTime, formatter)

        // Ensure start time is before end time
        val duration: Duration = if (start.isBefore(end)) {
            Duration.between(start, end)
        } else {
            Duration.between(end, start)
        }

        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60 // Calculate minutes remaining after hours
        return String.format("%02d:%02d", hours, minutes)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        b.btnCancel.setOnClickListener { dismiss() }

        b.btnAnnotate.setOnClickListener {
            val action =
                FragmentFlowDirections.actionFragmentFlowToFragmentUpdateDutyStatus(
                    currentData
                )
            requireActivity().findNavController(R.id.fragment_container_main)
                .navigate(action)
            dismiss()
        }
    }

    private fun setStatusColors(currentData: DataLog) {
        when (currentData.statusShort) {
            DutyStatus.ON_DUTY -> {
                setStatusIcon(
                    statusBg = R.color.status_on_color,
                    textColor = R.color.white_only
                )
            }

            DutyStatus.OFF_DUTY -> {
                setStatusIcon(
                    textColor = R.color.text_primary
                )
            }

            DutyStatus.SB -> {
                setStatusIcon(
                    statusBg = R.color.status_sb_color,
                    textColor = R.color.white_only
                )
            }

            DutyStatus.PC -> {
                setStatusIcon(
                    statusBg = R.color.status_pc_color,
                    textColor = R.color.white_only
                )
            }

            DutyStatus.YM -> {
                setStatusIcon(
                    statusBg = R.color.status_ym_color,
                    textColor = R.color.white_only
                )
            }

            DutyStatus.DR -> {
                setStatusIcon(
                    statusBg = R.color.success_on,
                    textColor = R.color.white_only
                )
            }

            DutyStatus.RESET -> {
                setStatusIcon(
                    statusBg = R.color.success_on,
                    textColor = R.color.white_only
                )
            }
        }

    }

    private fun setStatusIcon(
        statusBg: Int? = null,
        @ColorRes textColor: Int
    ) {
        b.tvStatus.setTextColor(ContextCompat.getColor(context, textColor))
        if (statusBg != null)
            b.tvStatus.backgroundTintList = ContextCompat.getColorStateList(context, statusBg)
    }
}