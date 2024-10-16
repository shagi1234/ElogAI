package com.selbiconsulting.elog.ui.main.main_log_page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.databinding.FragmentMainLogsBinding
import com.selbiconsulting.elog.databinding.FragmentUpdateMainInfoBinding
import com.selbiconsulting.elog.ui.util.ViewModelChangeStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentMainLogsPage : Fragment() {
    private lateinit var b: FragmentMainLogsBinding
    private val viewModelChangeStatus by activityViewModels<ViewModelChangeStatus>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelChangeStatus.getDriverDataLocal()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentMainLogsBinding.inflate(inflater, container, false)
        observe()
        return b.root
    }

    private fun observe() {
        lifecycleScope.launch {
            viewModelChangeStatus.driverData.collect {
                updateDriverInfo(it)
            }
        }
    }

    private fun updateDriverInfo(driver: EntityDriver?) = with(b) {
        if (driver == null) return
        tvDocId.text = driver.document
        tvTrailerId.text = driver.trailerId
        tvNote.text = driver.note
        tvDriverName.text = driver.name
        tvVehicleId.text = driver.vehicleId
        tvCarrierName.text = driver.carrier
        tvOfficeAddress.text = driver.mainTerminal
        tvTerminalAddress.text = driver.mainTerminal
    }


}