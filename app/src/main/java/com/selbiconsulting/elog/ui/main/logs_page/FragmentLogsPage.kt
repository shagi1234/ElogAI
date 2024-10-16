package com.selbiconsulting.elog.ui.main.logs_page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController

import androidx.recyclerview.widget.LinearLayoutManager
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentChildLogsBinding
import com.selbiconsulting.elog.ui.main.common.GraphViewHelper
import com.selbiconsulting.elog.ui.main.home.adapter.AdapterLogs
import com.selbiconsulting.elog.ui.main.home.adapter.LogsItemListener
import com.selbiconsulting.elog.ui.main.flow.FragmentFlowDirections
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.fromEntityLogToDataLogs
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.toDataLogs
import com.selbiconsulting.elog.ui.main.logs.ViewModelLogs
import com.selbiconsulting.elog.ui.util.ViewModelChangeStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentLogsPage : Fragment(), LogsItemListener {
    private lateinit var b: FragmentChildLogsBinding

    private val viewModelLogs: ViewModelLogs by activityViewModels()
    private val viewModelChangeStatus: ViewModelChangeStatus by activityViewModels()
    private lateinit var graphViewHelper: GraphViewHelper

    private lateinit var adapterLogs: AdapterLogs

    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper
    private lateinit var dateName: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = FragmentChildLogsBinding.inflate(inflater, container, false)

        graphViewHelper = GraphViewHelper(
            requireContext(),
            b.graphView
        )


        setRecyclerLogs()

        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observe()
        observeTimeByStatus()
    }

    private fun observeTimeByStatus() {
        lifecycleScope.launch {
            viewModelLogs.totalTimeOff.collect {
                b.valTotalTimeOff.text = it

                if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.OFF_DUTY) {
                    b.valTotalTimeLast.text = it
                }
            }
        }
        lifecycleScope.launch {
            viewModelLogs.totalTimeSB.collect {
                b.valTotalTimeSb.text = it

                if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.SB) {
                    b.valTotalTimeLast.text = it
                }

            }
        }
        lifecycleScope.launch {
            viewModelLogs.totalTimeOn.collect {
                b.valTotalTimeOn.text = it

                if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.ON_DUTY) {
                    b.valTotalTimeLast.text = it
                }
            }
        }
        lifecycleScope.launch {
            viewModelLogs.totalTimeDR.collect {
                b.valTotalTimeDr.text = it

                if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.DR) {
                    b.valTotalTimeLast.text = it
                }
            }
        }

    }

    private fun observe() {

        lifecycleScope.launch {
            viewModelLogs.dailyLogs.collect { dailyLogs ->
                val dataLogs = dailyLogs.fromEntityLogToDataLogs()
                viewModelLogs.getLineGraphSeries(dataLogs)
                b.signatureTakerButton.isEnabled = (dailyLogs.isNotEmpty())
                adapterLogs.updateData(dataLogs)
                viewModelLogs.calculateTimeByStatuses()
            }
        }

        lifecycleScope.launch {
            viewModelLogs.seriesLiveData.observe(viewLifecycleOwner) { series ->
                graphViewHelper.removeAllSeries()
                graphViewHelper.setSeriesSolid(series)
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModelLogs.selectedDate.collect { selectedDate ->
                dateName = selectedDate.formattedDate
            }
        }
    }

    private fun initListeners() = with(b) {
        signatureTakerButton.setOnClickListener {
            navigateToSingleCertifyLogFragment(dateName)
        }
        graphView.setOnClickListener {
            val navController = requireActivity().findNavController(R.id.fragment_container_main)
            navController.navigate(R.id.action_fragmentFlow_to_fragmentFullScreenGraph)
        }
    }

    private fun navigateToSingleCertifyLogFragment(dateName: String) {
        val action =
            FragmentFlowDirections.actionFragmentFlowToFragmentSingleCertifyLogs(
                date = dateName
            )
        requireActivity().findNavController(R.id.fragment_container_main).navigate(action)
    }


    private fun setRecyclerLogs() {
        adapterLogs = AdapterLogs(requireContext(), this)
        b.rvStatus.apply {
            adapter = adapterLogs
            layoutManager = LinearLayoutManager(context)
        }

    }


    override fun onLogsItemClicked(currentData: DataLog) {
        val bottomSheetLogDetails = BottomSheetLogDetails(requireContext(), currentData)
        bottomSheetLogDetails.show(childFragmentManager, bottomSheetLogDetails.tag)
    }
}