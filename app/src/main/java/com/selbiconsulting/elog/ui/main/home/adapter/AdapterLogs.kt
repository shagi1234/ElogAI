package com.selbiconsulting.elog.ui.main.home.adapter/*
 * Created by shagi on 12.02.2024 03:54
 */

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.model.enums.getShortDutyStatus
import com.selbiconsulting.elog.databinding.ItemTimelineRecyclerBinding
import com.selbiconsulting.elog.ui.util.DiffUtilCommon

class AdapterLogs(
    private val context: Context,
    private val listener: LogsItemListener? = null
) : RecyclerView.Adapter<AdapterLogs.ViewHolder>() {

    private var logs: MutableList<DataLog> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)

        return ViewHolder(
            ItemTimelineRecyclerBinding.inflate(inflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount(): Int {
        return logs.size
    }

    inner class ViewHolder(private val b: ItemTimelineRecyclerBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(currentData: DataLog) {
            b.status.text = getShortDutyStatus(currentData.statusShort)
            Log.e("CURRENT_DATA", "bind: ${currentData}", )
            b.item = currentData
            b.executePendingBindings()

            setStatusColors(currentData)

//            if (adapterPosition == itemCount - 1) {
//
//                b.line.visibility = View.GONE
//                b.divider.visibility = View.GONE
//            }

            b.root.setOnClickListener {
                listener?.onLogsItemClicked(currentData = currentData)
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

                DutyStatus.DR -> {
                    setStatusIcon(
                        statusBg = R.color.success_on,
                        textColor = R.color.white_only
                    )
                }

                DutyStatus.YM -> {
                    setStatusIcon(
                        statusBg = R.color.status_ym_color,
                        textColor = R.color.white_only
                    )
                }

                DutyStatus.RESET -> {
                    setStatusIcon(
                        statusBg = R.color.status_ym_color,
                        textColor = R.color.white_only
                    )
                }
            }
        }

        private fun setStatusIcon(
            statusBg: Int? = null,
            @ColorRes textColor: Int
        ) {
            b.status.setTextColor(ContextCompat.getColor(context, textColor))
            if (statusBg != null)
                b.status.backgroundTintList = ContextCompat.getColorStateList(context, statusBg)
        }
    }

    fun updateData(newLogs:List<DataLog>){
        logs.clear()
        logs.addAll(newLogs)
        Log.e("TAG___violationsss", "updateData: ${newLogs.lastOrNull()}")
        notifyDataSetChanged()
    }
}

interface LogsItemListener {
    fun onLogsItemClicked(currentData: DataLog)
}