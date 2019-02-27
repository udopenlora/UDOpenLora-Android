package com.sdc.udn.openlora.activity.adapter

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sdc.udn.openlora.R
import com.sdc.udn.openlora.activity.database.model.DeviceModel

class ListDeviceAdapter(private val context: Context) : RecyclerView.Adapter<ListDeviceAdapter.DeviceHolderView>() {
    var listDevices = mutableListOf<DeviceModel>()
    var listDevicesTemp = mutableListOf<DeviceModel>()
    private var eventListener: EventListener? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DeviceHolderView {
        val view = LayoutInflater.from(context).inflate(R.layout.item_device_view, p0, false)

        return DeviceHolderView(view)
    }

    override fun getItemCount(): Int {
        return listDevicesTemp.size
    }

    override fun onBindViewHolder(p0: DeviceHolderView, position: Int) {
        val device = listDevicesTemp[position]
        p0.display(device)

        p0.itemView.setOnClickListener {
            eventListener?.onClickDevice(device)
        }
    }

    fun updateDevice(device: DeviceModel) {
        if (listDevices.size == 0) {
            listDevices.add(device)
            listDevicesTemp = listDevices.filter { it.hadData }.toMutableList()
            notifyDataSetChanged()
            return
        }

        val deviceExits: DeviceModel? = listDevices.firstOrNull { it.id == device.id }

        if (deviceExits == null) {
            listDevices.add(device)
        } else {
            deviceExits.update(device)
        }

        listDevicesTemp = listDevices.filter { it.hadData }.toMutableList()
        notifyDataSetChanged()
    }

    fun removeDevice(device: DeviceModel) {
        val deviceExits = listDevices.firstOrNull { it.id == device.id } ?: return
        listDevices.remove(deviceExits)
        listDevicesTemp = listDevices.filter { it.hadData }.toMutableList()
        notifyDataSetChanged()
    }

    fun removeStatusDevice(device: DeviceModel) {
        val deviceExits: DeviceModel? = listDevices.firstOrNull { it.id == device.id }

        if (deviceExits != null) {
            deviceExits.status = null
            listDevicesTemp = listDevices.filter { it.hadData }.toMutableList()
            notifyDataSetChanged()
        }
    }

    fun setOnEventListener(event: EventListener) {
        this.eventListener = event
    }

    class DeviceHolderView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var lblName: AppCompatTextView = itemView.findViewById(R.id.lblName)
        private var viewAvailable: View = itemView.findViewById(R.id.viewAvailable)

        fun display(model: DeviceModel) {
            lblName.text = model.name
            viewAvailable.isEnabled = if (model.available == null) false else model.available!!
        }
    }

    interface EventListener {
        fun onClickDevice(device: DeviceModel)
    }
}