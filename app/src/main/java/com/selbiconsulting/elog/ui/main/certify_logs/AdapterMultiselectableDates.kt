package com.selbiconsulting.elog.ui.main.certify_logs

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.selbiconsulting.elog.databinding.ItemSelectableDateBinding

class AdapterSelectableDates(
    private val context: Context,
    private val items: List<DateItem>,
    private val listener: SelectableDatesItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var isSelectAllChecked = false
    private var selectedItemsCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val item = ItemSelectableDateBinding.inflate(inflater, parent, false)

        return ViewHolderDate(item)

    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolderDate).bind(items[position])
    }

    inner class ViewHolderDate(private val b: ItemSelectableDateBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(dateItem: DateItem) {
            b.checkboxDate.text = dateItem.title
            if (dateItem.isSelectAllItem == false) {
                b.checkboxDate.isChecked = dateItem.checked

            } else {
                b.checkboxDate.isChecked = isSelectAllChecked
            }

            b.checkboxDate.setOnCheckedChangeListener { _, isChecked ->
                if (dateItem.isSelectAllItem == true)
                    selectAllItems()
                else {
                    if (isSelectAllChecked && !isChecked) {
                        isSelectAllChecked = false
                        dateItem.checked = false
                        notifyItemChanged(adapterPosition)
                        notifyItemChanged(itemCount - 1)
                    }

                }

            }
        }
    }

    private fun selectAllItems() {
        isSelectAllChecked = !isSelectAllChecked
        items.forEach { dateItem ->
            dateItem.checked = isSelectAllChecked
        }
        notifyItemRangeChanged(0, itemCount - 1)
    }
}


interface SelectableDatesItemListener {
    fun onDateItemCheckedChanged(isChecked: Boolean, dateName: String)
}

data class DateItem(
    val title: String,
    var checked: Boolean,
    val isSelectAllItem: Boolean? = false
)