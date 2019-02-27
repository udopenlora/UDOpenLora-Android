package com.sdc.udn.openlora.activity.database.model

import com.google.firebase.database.DataSnapshot
import java.io.Serializable

class DeviceModel(val id: String) : Serializable {
    var name: String? = null
    var type: String? = null
    var status: Boolean? = null
    var available: Boolean? = null

    /**
     * Want to display a device on screen, device need have name, type and status.
     */
    val hadData get() = (name != null && type != null && status != null)

    constructor(id: String, name: String, type: String, available: Boolean) : this(id) {
        this.name = name
        this.type = type
        this.available = available
    }

    constructor(id: String, status: Boolean) : this(id) {
        this.status = status
    }

    /**
     * Update Device Info.
     */
    fun update(device: DeviceModel) {
        if (device.name != null) {
            this.name = device.name
        }

        if (device.type != null) {
            this.type = device.type
        }

        if (device.status != null) {
            this.status = device.status
        }

        if (device.available != null) {
            this.available = device.available
        }
    }

    companion object {
        /**
         * Key of Database.
         */
        const val NAME = "name"
        const val TYPE = "type"
        const val STATUS = "status"
        const val AVAILABLE = "available"

        /**
         * Parse Device Info.
         */
        fun InstanceDevice(data: DataSnapshot): DeviceModel? {
            val id = data.key ?: return null

            val values = data.value as? Map<*, *> ?: return null

            val name = values[NAME] as? String ?: return null
            val type = values[TYPE] as? String ?: return null
            val available = values[AVAILABLE] as? Boolean ?: return null

            return DeviceModel(id, name, type, available)
        }

        /**
         * Parse Device Status.
         */
        fun InstanceData(data: DataSnapshot): DeviceModel? {
            val id = data.key ?: return null
            val values = data.value as? Map<*, *> ?: return null
            val status = values[STATUS] as? Boolean ?: return null

            return DeviceModel(id, status)
        }
    }
}