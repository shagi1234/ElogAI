package com.selbiconsulting.elog.ui.main.settings.component

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.databinding.BottomSheetResetEldBinding
import com.selbiconsulting.elog.ui.main.certify_logs.components.BottomSheetSuccessModal
import com.selbiconsulting.elog.ui.main.common.CustomProgressDialog
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class BottomSheetRestartEld(private val listener: ResetEldListener?=null) :
    BottomSheetDialogFragment() {

    private lateinit var b: BottomSheetResetEldBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = BottomSheetResetEldBinding.inflate(LayoutInflater.from(context), container, false)
        initListeners()
        return b.root
    }

    private fun initListeners() {
        b.btnCancel.setOnClickListener { dismiss() }
        b.btnOk.setOnClickListener {
            listener?.onResetEldClicked()
            dismiss()

        }
    }
}

interface ResetEldListener {
    fun onResetEldClicked()
}

