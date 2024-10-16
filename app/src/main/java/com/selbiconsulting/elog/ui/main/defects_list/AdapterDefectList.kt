package com.selbiconsulting.elog.ui.main.defects_list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.selbiconsulting.elog.data.model.dto.DtoDefect
import com.selbiconsulting.elog.databinding.ItemDefectBinding

class AdapterDefectList(
    private val context: Context,
    private val defectsType: String,
    private val defects: List<DtoDefect>,
    private val checkedDefects: List<DtoDefect>,
    private val listener: OnDefectsItemCheckedListener
) : RecyclerView.Adapter<AdapterDefectList.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDefectBinding.inflate(
                LayoutInflater.from(
                    context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(defects[position])
    }

    override fun getItemCount(): Int {
        return defects.size
    }

    inner class ViewHolder(val b: ItemDefectBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(defect: DtoDefect) {
            b.checkboxDefects.text = defect.name
            b.checkboxDefects.isChecked = (defect in checkedDefects)
            if (b.checkboxDefects.isChecked)
                listener.onDefectChecked(
                    defectsType = defectsType,
                    defect = defect
                )
            else
                listener.onDefectUnchecked(
                    defectsType = defectsType,
                    defect = defect
                )

            b.checkboxDefects.setOnCheckedChangeListener { _, isChecked ->
                listener.onCheckedChange(
                    isChecked = isChecked,
                    defectsType = defectsType,
                    defect = defect
                )
            }
        }
    }
}

interface OnDefectsItemCheckedListener {
    fun onCheckedChange(isChecked: Boolean, defectsType: String, defect: DtoDefect)
    fun onDefectChecked(defectsType: String, defect: DtoDefect)
    fun onDefectUnchecked(defectsType: String, defect: DtoDefect)
}

