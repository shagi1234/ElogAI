package com.selbiconsulting.elog.ui.main.inspections.child

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.enums.getShortDutyStatus
import com.selbiconsulting.elog.databinding.ItemDailyLogsBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

class AdapterDailyLogs(private val context: Context, private val vehicle :String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var logs: List<EntityLog> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val logItem = ItemDailyLogsBinding.inflate(inflater, parent, false)
        return HolderDailyLog(logItem)
    }

    override fun getItemCount(): Int {
        return logs.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HolderDailyLog).bind(logs[position])
    }

    inner class HolderDailyLog(private val b: ItemDailyLogsBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(log: EntityLog) {
            b.tvLogNumber.text = "${adapterPosition + 1}"
            b.tvLogStatus.text = getShortDutyStatus(log.status)

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val start = LocalDateTime.parse(log.startTime, formatter)
            val end = LocalDateTime.parse(log.endTime, formatter)
            val duration = java.time.Duration.between(start, end)
            val hours = duration.toHours()
            val minutes = duration.minusHours(hours).toMinutes()
            b.tvLogDuration.text = String.format("%02d:%02d", hours, minutes)


            // Format the time as hh:mm
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val formattedStartTime = start.format(timeFormatter)

            // Set the formatted time to the TextView
            b.tvLogTime.text = formattedStartTime


            b.tvLogLocation.text = log.location
            b.tvLogVehicle.text = vehicle
            b.tvLogOdometer.text = log.odometer
            b.tvLogEngHours.text = log.engineHours
            b.tvLogNotes.text = log.note
        }

    }
}