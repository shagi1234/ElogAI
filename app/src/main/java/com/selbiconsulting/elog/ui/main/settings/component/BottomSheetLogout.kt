package com.selbiconsulting.elog.ui.main.settings.component

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.databinding.BottomSheetLogoutBinding

class BottomSheetLogout (
    private val listener: LogoutListener?=null
) : BottomSheetDialogFragment() {

    private lateinit var b:BottomSheetLogoutBinding

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
        b = BottomSheetLogoutBinding.inflate(inflater, container, false)
        b.btnCancel.setOnClickListener{
            dismiss()
        }
        b.btnOk.setOnClickListener {
            listener?.onLogoutClicked()
        }
        return b.root
    }
}

interface LogoutListener{
    fun onLogoutClicked()
}