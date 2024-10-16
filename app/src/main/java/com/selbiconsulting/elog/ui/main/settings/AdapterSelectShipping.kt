package com.selbiconsulting.elog.ui.main.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.databinding.ItemShippingDocumentBinding

class AdapterSelectShipping(val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var selectedPosition: Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(context)

        return ViewHolder(ItemShippingDocumentBinding.inflate(inflater, parent, false))
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind()
    }

    inner class ViewHolder(val b: ItemShippingDocumentBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind() {
            if (selectedPosition == adapterPosition) {
                setSelectedItem()
            } else {
                setUnselectedItem()

            }
            b.root.setOnClickListener {
                if (selectedPosition != adapterPosition) {
                    notifyItemChanged(selectedPosition)
                    selectedPosition = adapterPosition
                    setSelectedItem()
                    notifyItemChanged(selectedPosition)
                }
            }


        }

        private fun setUnselectedItem() {
            b.radioButton.isChecked = false
            b.root.backgroundTintList = context.getColorStateList(R.color.transparent)
        }

        private fun setSelectedItem() {
            b.radioButton.isChecked = true
            b.root.backgroundTintList = context.getColorStateList(R.color.secondary_surface)
        }

    }
}