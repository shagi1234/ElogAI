package com.selbiconsulting.elog.ui.main.certify_logs.components

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.request.RequestUploadFile
import com.selbiconsulting.elog.databinding.BottomSheetMultipleCertificationBinding
import com.selbiconsulting.elog.domain.use_case.RequestCertifyLogs
import com.selbiconsulting.elog.ui.main.certify_logs.AdapterSelectableDates
import com.selbiconsulting.elog.ui.main.certify_logs.DateItem
import com.selbiconsulting.elog.ui.main.certify_logs.OnCertifyLogsClicked
import com.selbiconsulting.elog.ui.main.certify_logs.SelectableDatesItemListener
import com.selbiconsulting.elog.ui.main.certify_logs.components.BottomSheetCertifyLogs.Companion.OPENED_FROM_FLOW
import com.selbiconsulting.elog.ui.main.flow.FragmentFlowDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.ceil

@AndroidEntryPoint
class BottomSheetMultipleCertification : BottomSheetDialogFragment(), SelectableDatesItemListener,
    OnCertifyLogsClicked {
    private lateinit var b: BottomSheetMultipleCertificationBinding
    private lateinit var adapterSelectableDates: AdapterSelectableDates
    private var selectedDatesItemsCount: Int = 0
    private var selectedSingleDateName: String = ""

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
        b = BottomSheetMultipleCertificationBinding.inflate(
            LayoutInflater.from(context),
            container,
            false
        )
        setRecyclerDates()
        return b.root
    }

    private fun setRecyclerDates() {
        var orderedItems: MutableList<DateItem>

        val items = listOf(
            DateItem("February 1", false),
            DateItem("February 2", false),
            DateItem("February 3", false),
            DateItem("February 4", false),
            DateItem("February 5", false),
            DateItem("February 6", false),
            DateItem("February 7", false),
            DateItem("Select All", false, true)
        )

        val orientation = resources.configuration.orientation
        val spanCount = if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
        val reOrderedItems = orderList(items, spanCount)

        adapterSelectableDates = AdapterSelectableDates(
            context = requireContext(),
            items = reOrderedItems,
            listener = this
        )

        b.rvDates.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = adapterSelectableDates
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        b.btnCancel.setOnClickListener { dismiss() }
        b.btnSave.setOnClickListener {
            if (selectedDatesItemsCount == 1)
                navigateToSingleCertifyLogFragment()
            else
                openBottomSheetCertifyLogs()
        }
    }

    private fun openBottomSheetCertifyLogs() {
        val bottomSheetCertifyLogs = BottomSheetCertifyLogs(/*penedFrom = OPENED_FROM_FLOW*/this)
        bottomSheetCertifyLogs.show(parentFragmentManager, bottomSheetCertifyLogs.tag)
        dismiss()
    }

    private fun navigateToSingleCertifyLogFragment() {
        val action =
            FragmentFlowDirections.actionFragmentFlowToFragmentSingleCertifyLogs(
                selectedSingleDateName
            )
        requireActivity().findNavController(R.id.fragment_container_main).navigate(action)
    }

    private fun orderList(originalList: List<DateItem>, numColumns: Int): List<DateItem> {
        if (numColumns == 2)
            return originalList
        val numRows = ceil(originalList.size.toDouble() / numColumns).toInt()
        val reorderedList = mutableListOf<DateItem>()

        for (col in 0 until numColumns) {
            for (row in 0 until numRows) {
                val index = row * numColumns + col
                if (index < originalList.size) {
                    reorderedList.add(originalList[index])
                }
            }
        }
        return reorderedList
    }

    override fun onDateItemCheckedChanged(isChecked: Boolean, dateName: String) {
        selectedDatesItemsCount =
            if (isChecked) selectedDatesItemsCount + 1 else selectedDatesItemsCount - 1
        selectedSingleDateName = dateName
    }

    override fun onResume() {
        super.onResume()
        selectedDatesItemsCount = 0
    }

    override fun uploadSignature(requestUploadFile: RequestUploadFile) {
        //
    }
}

