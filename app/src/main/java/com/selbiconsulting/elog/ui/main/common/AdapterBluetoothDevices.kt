package com.selbiconsulting.elog.ui.main.common

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iosix.eldblelib.EldScanObject
import com.selbiconsulting.elog.databinding.ItemBluetoothDeviceBinding

class AdapterBluetoothDevices(
    private val context: Context,
    private val listener: BluetoothDevicesListener?=null,
    private val devices: List<EldScanObject>? = null ,
    val bottomSheetScanBluetooth: BottomSheetScanBluetooth
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val item = ItemBluetoothDeviceBinding.inflate(inflater, parent, false)
        return HolderDevices(item)

    }

    override fun getItemCount(): Int = devices?.size ?: 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HolderDevices).bind()
    }

    inner class HolderDevices(private val b: ItemBluetoothDeviceBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind() {
            b.tvDeviceName.text = devices?.get(adapterPosition)?.deviceId

            b.root.setOnClickListener {
                listener?.onBluetoothDeviceItemClick(devices?.get(adapterPosition))
                bottomSheetScanBluetooth.dismiss()
            }
        }

    }
}

interface BluetoothDevicesListener {
    fun onBluetoothDeviceItemClick(get: EldScanObject?)
}