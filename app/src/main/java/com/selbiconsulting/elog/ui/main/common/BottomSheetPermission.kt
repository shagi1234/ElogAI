package com.selbiconsulting.elog.ui.main.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iosix.eldblelib.EldBleScanCallback
import com.iosix.eldblelib.EldManager
import com.iosix.eldblelib.EldScanObject
import com.selbiconsulting.elog.databinding.BottomSheetPermissionBinding

class BottomSheetPermission(
    private val listener: BluetoothDevicesListener?=null,
    private val bleScanCallback: EldBleScanCallback?=null,
    private val mEldManager: EldManager?=null
) : BottomSheetDialogFragment() {
    private lateinit var b: BottomSheetPermissionBinding
    private var bottomSheetScanBluetooth: BottomSheetScanBluetooth? = null

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
        b = BottomSheetPermissionBinding.inflate(inflater, container, false)
        //need to check permission bluetooth
        bottomSheetScanBluetooth = BottomSheetScanBluetooth(listener,bleScanCallback,mEldManager)
        initListeners()
        return b.root
    }

    fun showAvailableDevices(devices: List<EldScanObject>){
        bottomSheetScanBluetooth?.showAvailableDevices(devices)
    }
    fun showNotFoundLay(){
        bottomSheetScanBluetooth?.showNotFoundLay()
    }

    private fun initListeners() {
        b.btnOk.setOnClickListener {
            bottomSheetScanBluetooth?.show(parentFragmentManager, bottomSheetScanBluetooth?.tag)
            dismiss()

        }

        b.btnCancel.setOnClickListener {
            dismiss()
        }
    }
}

