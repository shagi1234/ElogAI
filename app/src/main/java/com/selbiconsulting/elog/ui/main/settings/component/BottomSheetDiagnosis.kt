package com.selbiconsulting.elog.ui.main.settings.component

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.BottomSheetDiagnosisBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BottomSheetDiagnosis(private val context: Context) : BottomSheetDialogFragment() {
    private lateinit var b: BottomSheetDiagnosisBinding

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

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
        b = BottomSheetDiagnosisBinding.inflate(LayoutInflater.from(context), container, false)
        setStatuses()
        initListeners()
        return b.root
    }

    private fun setStatuses() {
        if (isEldLocationWorking()) {
            setStatus(DiagnosisStatus.WORKING, b.tvEldCoordinatesStatus)
        } else {
            setStatus(DiagnosisStatus.NOT_WORKING, b.tvEldCoordinatesStatus)
        }

        if (isLocationServiceWorking()) {
            setStatus(DiagnosisStatus.WORKING, b.tvGpsStatus)
        } else {
            setStatus(DiagnosisStatus.NOT_WORKING, b.tvGpsStatus)
        }

        if (isEldLocationWorking() && isLocationServiceWorking()) {
            setStatus(DiagnosisStatus.WORKING, b.tvBothLocationStatus)
        } else {
            setStatus(DiagnosisStatus.NOT_WORKING, b.tvBothLocationStatus)
        }

        if (isInternetAvailable()) {
            setStatus(DiagnosisStatus.WORKING, b.tvInternetStatus)
        } else {
            setStatus(DiagnosisStatus.NOT_WORKING, b.tvInternetStatus)
        }

    }

    private fun isEldLocationWorking(): Boolean {
        return (sharedPreferencesHelper.isEldConnected && sharedPreferencesHelper.lastLongitude != 0f && sharedPreferencesHelper.lastLatitude != 0f)
    }


    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun isLocationServiceWorking(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return isGpsEnabled && hasLocationPermission
    }

    private fun setStatus(status: DiagnosisStatus, tvStatus: TextView) {
        if (status == DiagnosisStatus.WORKING) {
            tvStatus.text = context.resources.getString(R.string.good)
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.success_on))
            tvStatus.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.success_container)
        } else {
            tvStatus.text = context.resources.getString(R.string.not_working)
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.error_on))
            tvStatus.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.error_container)
        }

    }

    private fun initListeners() {
        b.btnDone.setOnClickListener { dismiss() }
    }
}

enum class DiagnosisStatus {
    WORKING,
    NOT_WORKING
}