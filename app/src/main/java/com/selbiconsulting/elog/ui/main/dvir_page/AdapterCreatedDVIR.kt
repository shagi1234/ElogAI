package com.selbiconsulting.elog.ui.main.dvir_page

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.dto.DtoDefect
import com.selbiconsulting.elog.data.model.entity.EntityDvir
import com.selbiconsulting.elog.databinding.ItemCreatedDvirBinding
import com.selbiconsulting.elog.ui.util.DiffUtilCommon
import com.selbiconsulting.elog.ui.util.UiHelper

class AdapterCreatedDVIR(
    private val context: Context,
    private val listener: CreatedDVIRItemListener,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val dvirItems: MutableList<EntityDvir> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val createdDvirItem = ItemCreatedDvirBinding.inflate(inflater, parent, false)
        return ViewHolderCreatedDvir(createdDvirItem)
    }

    override fun getItemCount(): Int {
        return dvirItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolderCreatedDvir).bind(dvirItems[position])
    }

    inner class ViewHolderCreatedDvir(private val b: ItemCreatedDvirBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(dvir: EntityDvir) {
            addTrailerDefects(dvir.trailerDefects)
            addUnitDefectsChips(dvir.unitDefects)

            if (dvir.mechanicSignature.isNullOrEmpty())
                setStatusWorkingOnIt()
            else
                setStatusFixed()

            b.dvir = dvir
            b.executePendingBindings()

            b.btnDelete.setOnClickListener {
                listener.onDeleteButtonClicked(dvir)
            }
            b.btnEdit.setOnClickListener {
                listener.onEditButtonClicked(dvir = dvir)
            }
        }

        private fun setStatusFixed() {
            b.tvStatus.text = context.resources.getString(R.string.fixed)
            b.tvStatus.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.success_container)
            b.tvStatus.setTextColor(ContextCompat.getColorStateList(context, R.color.success_on))
            b.btnEdit.visibility = View.GONE
        }

        private fun setStatusWorkingOnIt() {
            b.tvStatus.text = context.resources.getString(R.string.working_on_it)
            b.tvStatus.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.warning_container)
            b.tvStatus.setTextColor(ContextCompat.getColorStateList(context, R.color.warning_on))
            b.btnEdit.visibility = View.VISIBLE
        }

        private fun addTrailerDefects(trailerDefects: List<DtoDefect>?) {
            if (trailerDefects.isNullOrEmpty()) {
                b.trailerDefectsLay.visibility = View.GONE
                return
            }
            b.trailerDefectsLay.visibility = View.VISIBLE
            for (i in trailerDefects.indices) {
                val trailerDefectsChip = LayoutInflater.from(context).inflate(
                    R.layout.item_selected_defect_chip,
                    b.cpTrailerDefects,
                    false
                ) as Chip
                trailerDefectsChip.text = trailerDefects[i].name
                trailerDefectsChip.isCloseIconVisible = false
                trailerDefectsChip.chipEndPadding = UiHelper(context).dpToPx(12).toFloat()
                b.cpTrailerDefects.addView(trailerDefectsChip)
            }
        }

        private fun addUnitDefectsChips(unitDefects: List<DtoDefect>?) {
            if (unitDefects.isNullOrEmpty()) {
                b.unitDefectsLay.visibility = View.GONE
                return
            }
            b.unitDefectsLay.visibility = View.VISIBLE

            for (i in unitDefects.indices) {
                val unitDefectsChip = LayoutInflater.from(context).inflate(
                    R.layout.item_selected_defect_chip,
                    b.cpUnitDefects,
                    false
                ) as Chip
                unitDefectsChip.text = unitDefects[i].name
                unitDefectsChip.isCloseIconVisible = false
                unitDefectsChip.chipEndPadding = UiHelper(context).dpToPx(12).toFloat()
                b.cpUnitDefects.addView(unitDefectsChip)

            }

        }
    }

    fun updateData(dvirList: List<EntityDvir>) {
        val diffCallback = DiffUtilCommon(dvirItems, dvirList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        dvirItems.clear()
        dvirItems.addAll(dvirList)
        diffResult.dispatchUpdatesTo(this)
    }

}


interface CreatedDVIRItemListener {
    fun onDeleteButtonClicked(dvir: EntityDvir)

    fun onEditButtonClicked(dvir: EntityDvir)
}