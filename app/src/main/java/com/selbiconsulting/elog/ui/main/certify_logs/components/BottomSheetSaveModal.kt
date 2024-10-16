package com.selbiconsulting.elog.ui.main.certify_logs.components


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.dto.DtoDate
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.BottomSheetSaveModalBinding
import com.selbiconsulting.elog.ui.main.logs.ViewModelLogs
import com.selbiconsulting.elog.ui.util.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetSaveModal(private val openedFrom: Int) :
    BottomSheetDialogFragment() {
    private lateinit var b: BottomSheetSaveModalBinding

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModelLogs: ViewModelLogs by activityViewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper

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
        b = BottomSheetSaveModalBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        b.btnSave.setOnClickListener {
            sharedPreferences.encodedDriverSignature = sharedViewModel.encodedSignature.value
            if (openedFrom == BottomSheetCertifyLogs.OPENED_FROM_SINGLE_LOG) findNavController().navigate(
                R.id.action_fragmentSingleCertifyLogs_to_fragmentFlow
            )

            dismiss()

        }
        b.btnCancel.setOnClickListener {
            dismiss()
        }
    }
}