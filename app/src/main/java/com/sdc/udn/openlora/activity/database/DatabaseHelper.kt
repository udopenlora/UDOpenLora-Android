package com.sdc.udn.openlora.activity.database

import com.google.firebase.database.FirebaseDatabase
import com.sdc.udn.openlora.activity.database.model.LogDeviceModel


class DatabaseHelper {
    companion object {
        /**
         * Constructor 3 table Database.
         */
        val instance = FirebaseDatabase.getInstance()
        val Devices get() = instance.getReference("devices")
        val LogDevices get() = instance.getReference("device_log")
        val CurrentData get() = instance.getReference("current_data")

        /**
         * Update status and write log to database server when change status of device.
         */
        fun UpdateStatusDevice(
            status: Boolean,
            device: String,
            onSuccess: (Boolean) -> Unit,
            onFailed: (Exception) -> Unit
        ) {
            DatabaseHelper.CurrentData.child(device)
                .updateChildren(mutableMapOf("status" to status) as Map<String, Any>)
                .addOnCompleteListener {
                    // When update status is success, update new Log with now TimeStamp, current status of device.
                    DatabaseHelper.LogDevices.child(device)
                        .push()
                        .updateChildren(LogDeviceModel.Log(status))
                    onSuccess(status)
                }
                .addOnFailureListener { error ->
                    // Don't update Log, if update status is failed.
                    onFailed(error)
                }
        }
    }
}