package com.selbiconsulting.elog.ui.main.dvir_page

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.data.model.entity.EntityDvir
import com.selbiconsulting.elog.databinding.BottomSheetDeleteDvirBinding
import kotlinx.coroutines.launch

class BottomSheetDeleteDVIR(
    private val listener: BottomSheetDeleteDVIRListener?=null,
    private val currentDvir: EntityDvir
) :
    BottomSheetDialogFragment() {
    private lateinit var b: BottomSheetDeleteDvirBinding

    fun getProgressBar() = b.progressBar
    fun getDeleteButton() = b.btnDeleteReport

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
        b = BottomSheetDeleteDvirBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        b.btnCancel.setOnClickListener { dismiss() }
        b.btnDeleteReport.setOnClickListener {
            lifecycleScope.launch {
                listener?.onDeleteClicked(dvir = currentDvir)
            }
        }
    }
}

interface BottomSheetDeleteDVIRListener {
    fun onDeleteClicked(dvir: EntityDvir)
}