package com.selbiconsulting.elog.ui.main.settings.component

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.databinding.BottomSheetSendFeedbackBinding


class BottomSheetSendFeedback(
    private val context: Context,
    private val listener: SendFeedbackListener
) : BottomSheetDialogFragment() {

    private lateinit var b: BottomSheetSendFeedbackBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = BottomSheetSendFeedbackBinding.inflate(LayoutInflater.from(context), container, false)
        initListeners()
        return b.root
    }

    private fun initListeners() {
        b.edtFeedback.addTextChangedListener { editable ->
            b.btnSend.isEnabled = (editable?.isNotEmpty() == true)
        }
        b.btnSend.setOnClickListener {
            listener.onFeedbackClicked()
            dismiss()
        }
        b.btnCancel.setOnClickListener {
            dismiss()
        }
    }


}

interface SendFeedbackListener {
    fun onFeedbackClicked()
}
