package com.selbiconsulting.elog.ui.main.settings.component

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.databinding.BottomSheetRateUsBinding


class BottomSheetRateUs(private val context: Context) : BottomSheetDialogFragment() {

    private lateinit var b: BottomSheetRateUsBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = BottomSheetRateUsBinding.inflate(LayoutInflater.from(context), container, false)
        initListeners()
        return b.root
    }

    private fun initListeners() {
        b.btnCancel.setOnClickListener {
            dismiss()
        }
        b.ratingBar.setOnRatingChangeListener { _, rating, _ ->
            b.btnSubmit.isEnabled = (rating > 0)
        }
        b.btnSubmit.setOnClickListener {
            Log.e("RATING", "initListeners: ${b.ratingBar.rating}")
            dismiss()
        }
    }

}
