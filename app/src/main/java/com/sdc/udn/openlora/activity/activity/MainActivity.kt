package com.sdc.udn.openlora.activity.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.sdc.udn.openlora.R
import com.sdc.udn.openlora.activity.adapter.ListDeviceAdapter
import kotlinx.android.synthetic.main.activity_main.*
import com.sdc.udn.openlora.activity.database.DatabaseHelper
import com.sdc.udn.openlora.activity.database.model.DeviceModel
import android.content.Intent
import android.util.Log

class MainActivity : BaseActivity() {
    lateinit var deviceAdapter: ListDeviceAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponent()
        addListener()
    }

    /**
     * Setup View.
     */
    private fun initComponent() {
        setSupportActionBar(toolbar)
        listDevices.layoutManager = LinearLayoutManager(this)
        deviceAdapter = ListDeviceAdapter(this)
        listDevices.adapter = deviceAdapter
    }

    /**
     * Add Listener.
     */
    private fun addListener() {
        btnTalk.setOnClickListener {
            speechTalk()
        }

        // Listener Event from Device List: Click a device to open Log Device Screen.
        deviceAdapter.setOnEventListener(object : ListDeviceAdapter.EventListener {
            override fun onClickDevice(device: DeviceModel) {
                val intent = Intent(this@MainActivity, LogDeviceActivity::class.java)
                intent.putExtra("id", device.id)
                intent.putExtra("name", device.name)
                //intent.putExtra("status", device.status!!)
                startActivity(intent)
            }
        })

        // Listener changes of Device Table on server.
        DatabaseHelper.Devices.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            /**
             * When change info of a device.
             */
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val device = DeviceModel.InstanceDevice(p0) ?: return
                deviceAdapter.updateDevice(device)
            }

            /**
             * When a device added.
             */
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val device = DeviceModel.InstanceDevice(p0) ?: return
                deviceAdapter.updateDevice(device)
            }

            /**
             * When remove a device.
             */
            override fun onChildRemoved(p0: DataSnapshot) {
                val device = DeviceModel.InstanceDevice(p0) ?: return
                deviceAdapter.removeDevice(device)
            }

        })

        // Listener changes of Current Data Table on server.
        DatabaseHelper.CurrentData.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.v("MainActivity", p0.message)
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            /**
             * When change status of a device.
             */
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val device = DeviceModel.InstanceData(p0)

                if (device != null) {
                    deviceAdapter.updateDevice(device)
                }
            }

            /**
             * When added status of a device.
             */
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val device = DeviceModel.InstanceData(p0)

                if (device != null) {
                    deviceAdapter.updateDevice(device)
                }
            }

            /**
             * When remove status of a device.
             */
            override fun onChildRemoved(p0: DataSnapshot) {
                val device = DeviceModel.InstanceData(p0)

                if (device != null) {
                    deviceAdapter.removeStatusDevice(device)
                }
            }
        })
    }
}
