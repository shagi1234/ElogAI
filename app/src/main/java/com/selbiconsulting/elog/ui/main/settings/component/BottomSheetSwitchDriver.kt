package com.selbiconsulting.elog.ui.main.settings.component

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.databinding.BottomSheetSwitchDriverBinding


class BottomSheetSwitchDriver : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSwitchDriverBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSwitchDriverBinding.inflate(inflater, container, false)
        binding.btnAccept.setOnClickListener {
            BottomSheetSelectShipping().show(parentFragmentManager, BottomSheetSelectShipping().tag)
            dismiss()
        }
        binding.btnBack.setOnClickListener { dismiss() }
        return binding.root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
