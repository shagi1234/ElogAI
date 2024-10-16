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
import com.selbiconsulting.elog.databinding.BottomsheetSuccessModalBinding
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.main.logs.ViewModelLogs
import com.selbiconsulting.elog.ui.util.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BottomSheetSuccessModal(
    private val openedFrom: Int,
    private val sharedPreferences: SharedPreferencesHelper
) : BottomSheetDialogFragment() {
    private lateinit var b: BottomsheetSuccessModalBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModelLogs: ViewModelLogs by activityViewModels()


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
        b = BottomsheetSuccessModalBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        b.btnDone.setOnClickListener {

            if (sharedPreferences.encodedDriverSignature.isNullOrEmpty() || sharedPreferences.encodedDriverSignature.contentEquals(
                    sharedViewModel.encodedSignature.value
                )
            ) {
                sharedPreferences.encodedDriverSignature = sharedViewModel.encodedSignature.value

                if (openedFrom == BottomSheetCertifyLogs.OPENED_FROM_SINGLE_LOG)

                    findNavController().navigate(
                        R.id.action_fragmentSingleCertifyLogs_to_fragmentFlow
                    )

                CustomToast.showCustomToastWithContent(
                    v = it,
                    activity = requireActivity(),
                    state = ToastStates.SUCCESS,
                    "Success",
                    enableClearIcon = false
                )

                dismiss()

            } else {

                val bottomSheetSaveModal = BottomSheetSaveModal(openedFrom = openedFrom)
                bottomSheetSaveModal.show(parentFragmentManager, bottomSheetSaveModal.tag)
                dismiss()

            }

            lifecycleScope.launch(Dispatchers.IO) {
                viewModelLogs.selectedDate.emit(
                    DtoDate(
                        dayOfWeek = viewModelLogs.selectedDate.value.dayOfWeek,
                        day = viewModelLogs.selectedDate.value.day,
                        formattedDate = viewModelLogs.selectedDate.value.formattedDate,
                        isSelected = viewModelLogs.selectedDate.value.isSelected,
                        isCertified = true,
                        position = viewModelLogs.selectedDate.value.position
                    )
                )
            }

        }
    }
}