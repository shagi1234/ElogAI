package com.selbiconsulting.elog.ui.main.activity_main

import ViolationType
import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.ActivityMainBinding
import com.selbiconsulting.elog.di.App
import com.selbiconsulting.elog.ui.main.common.LocationAlertDialog
import com.selbiconsulting.elog.ui.util.Const
import com.selbiconsulting.elog.ui.util.NotificationData
import com.selbiconsulting.elog.ui.util.SharedViewModel
import com.selbiconsulting.elog.ui.util.ThemeManager
import com.selbiconsulting.elog.ui.util.UiHelper
import com.selbiconsulting.elog.ui.util.ViewModelChangeStatus
import com.selbiconsulting.elog.ui.util.receivers.StatusReceiver
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {
    private val ALARM_PERMISSION_REQUEST_CODE: Int = 21
    private lateinit var binding: ActivityMainBinding

    private val locationDialog: LocationAlertDialog by lazy { LocationAlertDialog(this) }
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var statusReceiver: StatusReceiver
    private val viewModel by viewModels<ViewModelMain>()
    private val sharedViewModel by viewModels<SharedViewModel>()
    private val viewModelChangeStatus by viewModels<ViewModelChangeStatus>()

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private val themeManager: ThemeManager by lazy { ThemeManager(this) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            checkBluetoothStatus()
        } else {
            finish()
        }
    }


    private var enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            checkLocationStatus()
        } else {
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        themeManager.setTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAlarmPermissions()
        createNotificationChannel()
        checkPermissionsAndSetup()
        registerStatusReceiver()
        handleKeepScreenMode()
    }

    private fun registerStatusReceiver() {
        statusReceiver = StatusReceiver(
            onLocationStatusChanged = { isTurnedOn ->
                if (isTurnedOn)
                    locationDialog.dismiss()
                else
                    locationDialog.show()

            },
            onBluetoothTurnedOff = { ->
                enableBluetooth()
            })


        val filter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        registerReceiver(statusReceiver, filter)

    }

    private fun checkLocationStatus() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            locationDialog.show()
        }
    }

    private fun checkBluetoothStatus() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled)
            enableBluetooth()
        else
            checkLocationStatus()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cancelAllNotification()

        Log.e("TAG_check_notifications", "onResume: ")
        viewModel.checkScheduledAlarms()
        Log.e("TAG_check_notifications", "onResume: ")
    }

    private fun checkPermissionsAndSetup() {
        val permissions = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION,
                BLUETOOTH
            )
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION,
                BLUETOOTH_CONNECT,
                BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION,
                BLUETOOTH_CONNECT,
                BLUETOOTH_SCAN,
                POST_NOTIFICATIONS
            )
        }

        val permissionsToRequest = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            checkBluetoothStatus()
        } else {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun createNotificationChannel() {
        val name = "ELOG_CHANNEL"
        val descriptionText = "SOME DESC"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(App.CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothLauncher.launch(enableBtIntent)

    }

    private fun handleKeepScreenMode() {
        val isKeepScreenEnabled = viewModel.isKeepScreenEnabled.value ?: false
        if (isKeepScreenEnabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }


    private fun calculateTimeMinus30Minutes(time: String): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Parse the input time string to a Date object
        val date = dateFormat.parse(time) ?: return "00:00"

        // Create a Calendar instance and set its time to the parsed date
        val calendar = Calendar.getInstance()
        calendar.time = date

        // Subtract 30 minutes from the calendar
        calendar.add(Calendar.MINUTE, -Const.lessThan30Mins)

        println("Salamalekum -Const.lessThan30Seconds.toInt() ${-Const.lessThan30Mins.toInt()}")

        // Return the formatted time as a string
        return dateFormat.format(calendar.time)
    }

    override fun onPause() {
        super.onPause()
        val remainingTimeDR = viewModelChangeStatus.calculateDriveTime()
        val remainingTimeShift = viewModelChangeStatus.calculateShiftTime()
        val remainingTimeBreak = viewModelChangeStatus.calculateBreakTime()

        if (remainingTimeDR != "00:00") {
            val timeCalculated = calculateTimeMinus30Minutes(remainingTimeDR)
            val notificationDR = NotificationData(
                "Ending Driving",
                timeCalculated,
                ViolationType.WARNING_30_MINUTE_BEFORE_DRIVING_ENDS.message
            )
            viewModel.scheduleNotifications(notificationDR)
        }

        if (remainingTimeShift != "00:00") {
            val timeCalculated = calculateTimeMinus30Minutes(remainingTimeShift)
            val notificationShift = NotificationData(
                "Ending Shift",
                timeCalculated,
                ViolationType.WARNING_30_MINUTE_BEFORE_DRIVING_ENDS.message
            )
            viewModel.scheduleNotifications(notificationShift)
        }

        if (remainingTimeBreak != "00:00") {
            val timeCalculated = calculateTimeMinus30Minutes(remainingTimeBreak)
            val notificationBreak = NotificationData(
                "Break Ending",
                timeCalculated,
                ViolationType.WARNING_30_MINUTE_BEFORE_DRIVING_ENDS.message
            )
            viewModel.scheduleNotifications(notificationBreak)
        }

        Log.e("TAG_check_notifications", "onPause: ")
        viewModel.checkScheduledAlarms()
        Log.e("TAG_check_notifications", "onPause: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(statusReceiver)
    }

    private fun checkAlarmPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.SET_ALARM,
                Manifest.permission.USE_EXACT_ALARM,
                Manifest.permission.VIBRATE,
                POST_NOTIFICATIONS  // Add your custom permission here
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.SET_ALARM,
                Manifest.permission.VIBRATE,
                Manifest.permission.SCHEDULE_EXACT_ALARM,
            )
        } else {
            arrayOf(
                Manifest.permission.SET_ALARM, Manifest.permission.VIBRATE
            )
        }

        val permissionsToRequest = mutableListOf<String>()

        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            Log.e("TAG_check_notifications", "checkAlarmPermissions: permissions denied")

            ActivityCompat.requestPermissions(
                this, permissionsToRequest.toTypedArray(), ALARM_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (sharedPreferencesHelper.currentVolume > 0) {
                sharedPreferencesHelper.currentVolume -= 1
                sharedViewModel.volume.value = sharedPreferencesHelper.currentVolume
            }
        } else {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (sharedPreferencesHelper.currentVolume < maxVolume) {
                sharedPreferencesHelper.currentVolume += 1
                sharedViewModel.volume.value = sharedPreferencesHelper.currentVolume
            }
        }
        return true
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            UiHelper(this).hideKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }
}




