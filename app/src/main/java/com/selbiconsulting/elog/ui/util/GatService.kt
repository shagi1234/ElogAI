package com.selbiconsulting.elog.ui.util


/*
 * Created by shagi on 22.03.2024 18:34
 */

/*
 * Copyright (c) 2020, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import no.nordicsemi.android.ble.BleManager
import java.nio.charset.Charset
import java.util.*
import androidx.annotation.NonNull

import android.bluetooth.BluetoothDevice
import com.selbiconsulting.elog.R

import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.data.Data


/**
 * Connects with a Bluetooth LE GATT service and takes care of its notifications. The service
 * runs as a foreground service, which is generally required so that it can run even
 * while the containing app has no UI. It is also possible to have the service
 * started up as part of the OS boot sequence using code similar to the following:
 *
 * <pre>
 *     class OsNotificationReceiver : BroadcastReceiver() {
 *          override fun onReceive(context: Context?, intent: Intent?) {
 *              when (intent?.action) {
 *                  // Start our Gatt service as a result of the system booting up
 *                  Intent.ACTION_BOOT_COMPLETED -> {
 *                     context?.startForegroundService(Intent(context, GattService::class.java))
 *                  }
 *              }
 *          }
 *      }
 * </pre>
 */
class GattService : Service() {

    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val handler: Handler = Handler()
    private var runnable: Runnable? = null

    private lateinit var stateChangedObserver: BroadcastReceiver
    private lateinit var bondStateObserver: BroadcastReceiver

    private var myCharacteristicChangedChannel: SendChannel<String>? = null 

    private val clientManagers = mutableMapOf<String, ClientManager>()

    override fun onCreate() {
        super.onCreate()

        // Setup as a foreground service

        val notificationChannel = NotificationChannel(
            GattService::class.java.simpleName,
            "ElogAi",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationService =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationService.createNotificationChannel(notificationChannel)

        val notification = NotificationCompat.Builder(this, GattService::class.java.simpleName)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("ElogAi")
            .setContentText("ElogAi running bluetooth")
            .setAutoCancel(true)

        startForeground(1, notification.build())

        // Observe OS state changes in BLE

        stateChangedObserver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val bluetoothState = intent!!.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    -1
                )
                when (bluetoothState) {
                    BluetoothAdapter.STATE_ON -> enableBleServices()
                    BluetoothAdapter.STATE_OFF -> disableBleServices()
                }
            }
        }
        bondStateObserver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val device =
                    intent!!.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Log.d(TAG, "Bond state changed for device ${device?.address}: ${device?.bondState}")

                Log.e("TAG_blll", "onReceive: ${device?.name}")
                when (device?.bondState) {
                    BluetoothDevice.BOND_BONDED -> addDevice(device)
                    BluetoothDevice.BOND_NONE -> removeDevice(device)
                }
            }
        }

        ContextCompat.registerReceiver(
            this,
            stateChangedObserver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )
        ContextCompat.registerReceiver(
            this,
            bondStateObserver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )

        // Startup BLE if we have it
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter?.isEnabled == true) enableBleServices()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(stateChangedObserver)
        unregisterReceiver(bondStateObserver)
        disableBleServices()
    }

    override fun onBind(intent: Intent?): IBinder? =
        when (intent?.action) {
            DATA_PLANE_ACTION -> {
                DataPlane()
            }

            else -> null
        }

    override fun onUnbind(intent: Intent?): Boolean =
        when (intent?.action) {
            DATA_PLANE_ACTION -> {
                myCharacteristicChangedChannel = null
                true
            }

            else -> false
        }

    /**
     * A binding to be used to interact with data of the service
     */
    inner class DataPlane : Binder() {
        fun setMyCharacteristicChangedChannel(sendChannel: SendChannel<String>) {
            myCharacteristicChangedChannel = sendChannel
        }
    }

    private fun enableBleServices() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter?.isEnabled == true) {

            bluetoothManager.adapter.bondedDevices.forEach { device ->
                Log.e(TAG, "enableBleServices: ${device?.name}")
                addDevice(device)
            }

        } else {
            Log.w(
                TAG,
                "Cannot enable BLE services as either there is no Bluetooth adapter or it is disabled"
            )
        }
    }

    private fun disableBleServices() {
        clientManagers.values.forEach { clientManager ->
            clientManager.close()
        }
        clientManagers.clear()
    }

    private fun addDevice(device: BluetoothDevice) {
        if (!clientManagers.containsKey(device.address)) {
            val clientManager = ClientManager()
            clientManager.connect(device).useAutoConnect(true).enqueue()
            clientManagers[device.address] = clientManager

        }
    }

    private fun removeDevice(device: BluetoothDevice) {
        clientManagers.remove(device.address)?.close()
    }

    /*
     * Manages the entire GATT service, declaring the services and characteristics on offer
     */
    companion object {
        /**
         * A binding action to return a binding that can be used in relation to the service's data
         */
        const val DATA_PLANE_ACTION = "data-plane"

        private const val TAG = "gatt-service"
    }

    private inner class ClientManager : BleManager(this@GattService) {
        private var myCharacteristic: BluetoothGattCharacteristic? = null

        override fun log(priority: Int, message: String) {

            Log.e("", message)
            Log.e("", " channel " + myCharacteristicChangedChannel?.toString())
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(MyServiceProfile.MY_SERVICE_UUID)

            myCharacteristic = service?.getCharacteristic(MyServiceProfile.MY_CHARACTERISTIC_UUID)

            val myCharacteristicProperties = myCharacteristic?.properties ?: 0

            Log.e(TAG, "isRequiredServiceSupported: ${myCharacteristic?.uuid}")


            return true
        }

        fun decodeByteArray(byteArray: ByteArray, charset: Charset): String {
            return byteArray.toString(charset)
        }

        fun wagtly_wagtyna() {
            runnable = Runnable {
                readCharacteristic(myCharacteristic)
                    .done {
//                        myCharacteristic.descriptors.
                        wagtly_wagtyna()
                    }
                    .enqueue()
            }
            handler.postDelayed(runnable!!, 3000)

        }

        override fun initialize() {

            readCharacteristic(myCharacteristic)
                .with { device, data ->
                    Log.e(TAG, "initialize: $data")
                    Log.e(TAG, "initialize: $device")

                }.done {
                    log(Log.INFO, "Target data")
                    wagtly_wagtyna()
                }

            beginAtomicRequestQueue()
                .add(readCharacteristic(myCharacteristic)
                    .with { device, data ->
                        Log.e(TAG, "initialize: $data")
                        Log.e(TAG, "initialize: $device")

                    }.done {
                        log(Log.INFO, "Target dsdsdata")
                        wagtly_wagtyna()
                    }
                )
                .done {
                    log(Log.INFO, "Target data")
                    wagtly_wagtyna()
                }
                .enqueue()


        }

        override fun onServicesInvalidated() {
            myCharacteristic = null
        }
    }

    object MyServiceProfile {
        val MY_SERVICE_UUID: UUID = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb")
        val MY_CHARACTERISTIC_UUID: UUID = UUID.fromString("72563044-DB33-4692-A45D-C5212EEBABFA")
    }
}