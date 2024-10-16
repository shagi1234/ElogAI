package com.selbiconsulting.elog.ui.util.receivers

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.media3.common.MediaItem
import com.selbiconsulting.elog.ui.main.common.BottomSheetPermission
import com.selbiconsulting.elog.ui.main.common.LocationAlertDialog

class StatusReceiver(
    private val onBluetoothTurnedOff: () -> Unit,
    private val onLocationStatusChanged: (isTurnedOn: Boolean) -> Unit,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            LocationManager.PROVIDERS_CHANGED_ACTION -> {
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                onLocationStatusChanged(isGpsEnabled && isNetworkEnabled)
            }

            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val bluetoothState =
                    intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                if (bluetoothState == BluetoothAdapter.STATE_OFF)
                    onBluetoothTurnedOff()
            }
        }
    }

}