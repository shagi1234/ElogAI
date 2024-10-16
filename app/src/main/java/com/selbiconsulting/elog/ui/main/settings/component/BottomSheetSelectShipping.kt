package com.selbiconsulting.elog.ui.main.settings.component

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.databinding.BottomSheetSelectShippingDriverBinding
import com.selbiconsulting.elog.ui.main.settings.AdapterSelectShipping

class BottomSheetSelectShipping : BottomSheetDialogFragment() {
    private lateinit var b: BottomSheetSelectShippingDriverBinding
    private lateinit var adapterSelectShipping: AdapterSelectShipping

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
        b = BottomSheetSelectShippingDriverBinding.inflate(inflater, container, false)
        setRecyclerWithRadioButton()
        initListeners()
        return b.root
    }

    private fun setRecyclerWithRadioButton() {
        adapterSelectShipping = AdapterSelectShipping(requireContext())
        b.rvRadioButtons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterSelectShipping
        }
    }

    private fun initListeners() {
        b.btnCancel.setOnClickListener {
            dismiss()
        }

        b.btnOk.setOnClickListener { dismiss() }

    }
}