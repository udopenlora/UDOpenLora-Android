package com.sdc.udn.openlora.activity.database.model

import com.google.firebase.database.DataSnapshot
import java.text.SimpleDateFormat
import java.util.*

class LogDeviceModel(val timeStamp: Long, val sourceControl: String, val status: Boolean) {
    /**
     * Convert time which save in Log to string.
     */
    fun dateString(): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeStamp
        val sdf = SimpleDateFormat("HH:mm:ss dd-MM-yyyy")

        return sdf.format(cal.time)
    }

    companion object {
        /**
         * Key of Database.
         */
        const val TIME_STAMP = "timestamp"
        const val SOURCE_CONTROL = "source_control"
        const val STATUS = "status"
        const val DATA = "data"
        const val SOURCE = "Android"

        /**
         * Parse Device Log.
         */
        fun Instance(data: DataSnapshot): LogDeviceModel? {
            val values = data.value as? Map<*, *> ?: return null

            val timeStamp = values[TIME_STAMP] as? Long ?: return null
            val sourceControl = values[SOURCE_CONTROL] as? String ?: return null
            val valuesData = values[DATA] as? Map<*, *> ?: return null
            val status = valuesData[STATUS] as? Boolean ?: return null

            return LogDeviceModel(timeStamp, sourceControl, status)
        }

        /**
         * Create Log with current status of device.
         */
        fun Log(status: Boolean): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            map[LogDeviceModel.SOURCE_CONTROL] = LogDeviceModel.SOURCE
            map[LogDeviceModel.TIME_STAMP] = System.currentTimeMillis()
            map[LogDeviceModel.DATA] = mutableMapOf(LogDeviceModel.STATUS to status)

            return map
        }
    }
}