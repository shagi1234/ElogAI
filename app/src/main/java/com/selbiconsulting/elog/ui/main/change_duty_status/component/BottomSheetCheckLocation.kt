package com.selbiconsulting.elog.ui.main.change_duty_status.component

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.databinding.BottomSheetCheckLocationBinding
import com.selbiconsulting.elog.ui.main.common.CustomProgressDialog
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog
import com.selbiconsulting.elog.ui.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Singleton


class BottomSheetCheckLocation(
    private val context: Context,
    private val listener: LocationListener? = null,
    private val locationName: MutableStateFlow<String> = MutableStateFlow("N/A")
) : BottomSheetDialogFragment() {
    private lateinit var b: BottomSheetCheckLocationBinding

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
        b = BottomSheetCheckLocationBinding.inflate(LayoutInflater.from(context), container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            locationName.collect {
                lifecycleScope.launch {
                    b.tvLocation.text = it
                    delay(500)
                    if (b.tvLocation.text.isNotEmpty() && !b.tvLocation.text.equals("N/A"))
                        dismiss()
                }
            }

        }
    }

    private fun initListeners() {
        b.btnCancel.setOnClickListener { dismiss() }
        b.btnUpdate.setOnClickListener {
            listener?.updateLocation()
        }

    }


}

interface LocationListener {
    fun updateLocation()
}