package com.selbiconsulting.elog.ui.main.change_duty_status.component

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.databinding.BottomSheetSaveDutyStatusBinding

class BottomSheetSaveChangedStatusStatus(
    private val context: Context,
    private val listener: BottomSheetSaveDutyStatusListener
) :
    BottomSheetDialogFragment() {
    private lateinit var b: BottomSheetSaveDutyStatusBinding

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
        b = BottomSheetSaveDutyStatusBinding.inflate(LayoutInflater.from(context), container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        b.btnCancel.setOnClickListener { dismiss() }
        b.btnSave.setOnClickListener {
            listener.onSaveButtonClicked()
            dismiss()
        }
    }
}

interface BottomSheetSaveDutyStatusListener {
    fun onSaveButtonClicked()
}