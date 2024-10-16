package com.selbiconsulting.elog.ui.main.logs_page

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.dto.DtoDate
import com.selbiconsulting.elog.databinding.ItemDateLogsBinding
import com.selbiconsulting.elog.ui.util.UiHelper
import java.util.Random

class AdapterDateLogs(
    private val context: Context,
    private val listener: DateItemListener? = null
) : RecyclerView.Adapter<AdapterDateLogs.ViewHolder>() {

    var selectedPosition = 0

    var dates: List<DtoDate>? = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
            selectedPosition = itemCount - 1
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDateLogsBinding.inflate(
                LayoutInflater.from(
                    context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (dates.isNullOrEmpty()) return
        holder.bind(dates!![position])
    }

    override fun getItemCount(): Int {
        return dates?.size ?: 0
    }

    inner class ViewHolder(val b: ItemDateLogsBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(currentDate: DtoDate) {
            b.item = currentDate
            b.executePendingBindings()

            currentDate.position = adapterPosition
            setIsCertified(currentDate.isCertified)

            if (selectedPosition == adapterPosition) {
                setSelectedItem()
                listener?.onDateClicked(currentDate)
            } else setUnselectedItem()

            b.root.setOnClickListener {

                if (selectedPosition != adapterPosition) {
                    notifyItemChanged(selectedPosition, Unit)

                    selectedPosition = adapterPosition
                    setSelectedItem()

                    notifyItemChanged(selectedPosition, Unit)
                }

                listener?.onDateClicked(currentDate)
            }
        }

        private fun setSelectedItem(hasViolation: Boolean = false) {
            val selectedColor =
                if (hasViolation) context.getColor(R.color.error_on) else context.getColor(R.color.primary_brand)
            b.tvDayOfWeek.setTextColor(selectedColor)
            b.tvDayOfMonths.setTextColor(selectedColor)
            b.dateBackground.setBackgroundColor(context.getColor(R.color.on_primary))
            b.itemCard?.strokeWidth = UiHelper(context).dpToPx(1)

        }

        private fun setUnselectedItem(hasViolation: Boolean = false) {
            val unSelectedColor =
                if (hasViolation) context.getColor(R.color.error_on) else context.getColor(R.color.text_secondary)
            b.tvDayOfWeek.setTextColor(unSelectedColor)
            b.tvDayOfMonths.setTextColor(unSelectedColor)
            b.dateBackground.setBackgroundColor(context.getColor(R.color.secondary_surface))

            b.itemCard?.strokeWidth = UiHelper(context).dpToPx(0)
        }

        private fun setIsCertified(isCertified: Boolean) {
            if (isCertified) {
                b.tvIsCertified.text = context.resources.getString(R.string.certified)
                b.tvIsCertified.setTextColor(context.getColor(R.color.text_secondary))

            } else {
                b.tvIsCertified.text = context.resources.getString(R.string.not_certified)
                b.tvIsCertified.setTextColor(context.getColor(R.color.warning_on))

            }
        }
    }
}

interface DateItemListener {
    fun onDateClicked(date: DtoDate)
}