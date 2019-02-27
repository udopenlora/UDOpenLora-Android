package com.sdc.udn.openlora.activity.activity

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.Menu
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.sdc.udn.openlora.R
import com.sdc.udn.openlora.activity.database.DatabaseHelper
import android.view.MenuItem
import android.view.View
import com.sdc.udn.openlora.activity.adapter.LogDeviceAdapter
import com.sdc.udn.openlora.activity.database.model.DeviceModel
import com.sdc.udn.openlora.activity.database.model.LogDeviceModel
import kotlinx.android.synthetic.main.activity_log_device.*

class LogDeviceActivity : BaseActivity() {
    private lateinit var idDevice: String
    private lateinit var nameDevice: String
    var btnControl: SwitchCompat? = null
    var menuControl: MenuItem? = null
    var viewAvailable: View? = null
    var statusDevice: Boolean? = null
    private var availableDevice: Boolean = false
    lateinit var logAdapter: LogDeviceAdapter
    private var eventListener: ChildEventListener? = null
    private var limitItem =  0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_device)

        idDevice = intent.getStringExtra("id")
        nameDevice = intent.getStringExtra("name")

        initComponent()
        addListener()
    }

    /**
     * Setup View.
     */
    private fun initComponent() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = nameDevice
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val layoutManager = LinearLayoutManager(this)
        listDevices.layoutManager = layoutManager
        logAdapter = LogDeviceAdapter(this)
        listDevices.adapter = logAdapter
    }

    /**
     * Load device log.
     */
    private fun loadMoreLog() {
        // Check number of Log greater limit to load more.
        if (logAdapter.itemCount >= limitItem - 1) {
            limitItem += 20
        } else {
            return
        }

        if (eventListener != null) {
            // Remove old listener with old limit.
            DatabaseHelper.LogDevices.child(idDevice).removeEventListener(eventListener!!)
        }

        // Listener changes of Device Log from server.
        eventListener = DatabaseHelper.LogDevices.child(idDevice)
            .orderByChild(LogDeviceModel.TIME_STAMP)
            .limitToLast(limitItem)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    //val log = LogDeviceModel.Instance(p0) ?: return
                }

                /**
                 * When a log device added.
                 */
                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val log = LogDeviceModel.Instance(p0) ?: return
                    logAdapter.updateLog(log)
                }

                /**
                 * When a device removed.
                 */
                override fun onChildRemoved(p0: DataSnapshot) {
                    val log = LogDeviceModel.Instance(p0) ?: return
                    logAdapter.removeLog(log)
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.v("LogDeviceActivity", p0.message)
                }
            })
    }

    /**
     * Add Listener.
     */
    private fun addListener() {
        btnTalk.setOnClickListener {
            speechTalk()
        }

        // Listener when user swipe bottom of list to load more log.
        loadmoreView.setOnRefreshListener {
            loadMoreLog()
            loadmoreView.isRefreshing = false
        }

        // Load Log Device from Server.
        loadMoreLog()

        // Listener changes of Current Data Table on server.
        DatabaseHelper.CurrentData.child(idDevice).addChildEventListener(object : ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val status: Boolean? = p0.value as? Boolean

                if (status != null) {
                    statusDevice = status
                    menuControl?.isVisible = true
                    btnControl?.isChecked = status
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val status: Boolean? = p0.value as? Boolean

                if (status != null) {
                    statusDevice = status
                    menuControl?.isVisible = true
                    btnControl?.isChecked = status
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                menuControl?.isVisible = false
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        // Listener changes of Device Table on server.
        DatabaseHelper.Devices.child(idDevice).addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                updateDeviceInfo(p0)
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                updateDeviceInfo(p0)
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    /**
     * Parse Info Device and Update to UI.
     */
    fun updateDeviceInfo(data: DataSnapshot) {
        val key = data.key ?: return

        when(key) {
            DeviceModel.NAME -> {
                val name = data.value as? String ?: return
                nameDevice = name
                supportActionBar?.title = nameDevice
            }

            DeviceModel.TYPE -> {

            }

            DeviceModel.AVAILABLE -> {
                val available = data.value as? Boolean ?: return
                availableDevice = available
                viewAvailable?.isEnabled = availableDevice
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_log, menu)
        menuControl = menu?.findItem(R.id.btnControl)

        btnControl = menuControl?.actionView as SwitchCompat
        viewAvailable = menu?.findItem(R.id.viewAvailable)?.actionView
        viewAvailable?.isEnabled = availableDevice

        if (statusDevice == null) {
            menuControl?.isVisible = false
        } else {
            menuControl?.isVisible = true
            btnControl?.isChecked = statusDevice!!
        }

        // Listener changes of Status Swtich.
        btnControl?.setOnClickListener {
            // Update Status Device.
            DatabaseHelper.UpdateStatusDevice(btnControl!!.isChecked, idDevice, { status ->
                // Update status variable, if update status is success.
                statusDevice = status
            }, {
                // If update status failed, show Error alert.
                showAlertDialog(String.format("Cập nhật trạng thái thiết bị %s thất bại", nameDevice))
                btnControl?.isChecked = statusDevice!!
            })
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Show Alert Dialog.
     */
    private fun showAlertDialog(message: String) {
        val builder1 = AlertDialog.Builder(this)
        builder1.setMessage(message)
        builder1.setCancelable(true)

        builder1.setNegativeButton(
            "Đóng"
        ) { dialog, _ -> dialog.cancel() }

        val alert11 = builder1.create()
        alert11.show()
    }
}