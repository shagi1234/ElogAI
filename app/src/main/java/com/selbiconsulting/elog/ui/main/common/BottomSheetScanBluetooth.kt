package com.selbiconsulting.elog.ui.main.common

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iosix.eldblelib.EldBleError
import com.iosix.eldblelib.EldBleScanCallback
import com.iosix.eldblelib.EldManager
import com.iosix.eldblelib.EldScanObject
import com.selbiconsulting.elog.databinding.BottomSheetScanBluetoothBinding
import com.selbiconsulting.elog.ui.main.home.FragmentHome.Companion.REQUEST_BT_ENABLE
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BottomSheetScanBluetooth(
    private val listener: BluetoothDevicesListener?=null,
    private val bleScanCallback: EldBleScanCallback?=null,
    private val mEldManager: EldManager?=null
) : BottomSheetDialogFragment() {

    private lateinit var b: BottomSheetScanBluetoothBinding
    private var adapterBluetoothDevices: AdapterBluetoothDevices? = null

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
        b = BottomSheetScanBluetoothBinding.inflate(LayoutInflater.from(context), container, false)

        initListeners()
        lifecycleScope.launch {

            if (mEldManager?.ScanForElds(bleScanCallback) == EldBleError.BLUETOOTH_NOT_ENABLED) {
                Log.e("BLETEST", "BLUETOOTH_NOT_ENABLED" )
                mEldManager.EnableBluetooth(REQUEST_BT_ENABLE)
            }

            scanDevices()
        }
        return b.root
    }


    private fun setRecyclerDevices(devices: List<EldScanObject>) {
        adapterBluetoothDevices = AdapterBluetoothDevices(
            requireContext(),
            listener,
            devices,
            this
        )
        b.rvDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterBluetoothDevices
        }

    }

    private fun initListeners() {
        b.btnScan.setOnClickListener {
            lifecycleScope.launch {
                if (mEldManager?.ScanForElds(bleScanCallback) == EldBleError.BLUETOOTH_NOT_ENABLED) {
                    mEldManager.EnableBluetooth(REQUEST_BT_ENABLE)
                }
                scanDevices()
            }
        }
    }


    private fun scanDevices() {
        b.btnScan.isEnabled = false
        b.btnScan.text = "Search for devices"
        b.progressLay.visibility = View.VISIBLE
        b.devicesLay.visibility = View.GONE
        b.notFoundLay.visibility = View.GONE
    }

    fun showAvailableDevices(devices: List<EldScanObject>) {
        b.btnScan.isEnabled = true
        b.btnScan.text = "Search for devices"
        b.devicesLay.visibility = View.VISIBLE
        b.progressLay.visibility = View.GONE
        b.notFoundLay.visibility = View.GONE

        if (devices.isEmpty()) {
            showNotFoundLay()
        } else
            setRecyclerDevices(devices)
    }

    fun showNotFoundLay() {
        b.btnScan.isEnabled = true
        b.btnScan.text = "Try Again"
        b.notFoundLay.visibility = View.VISIBLE
        b.progressLay.visibility = View.GONE
        b.progressLay.visibility = View.GONE
    }

}