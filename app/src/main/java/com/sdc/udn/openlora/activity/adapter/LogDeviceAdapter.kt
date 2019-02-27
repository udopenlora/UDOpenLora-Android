package com.sdc.udn.openlora.activity.adapter

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sdc.udn.openlora.R
import com.sdc.udn.openlora.activity.database.model.LogDeviceModel

class LogDeviceAdapter(private val context: Context) : RecyclerView.Adapter<LogDeviceAdapter.LogViewHolder>() {
    private var listLogs = mutableListOf<LogDeviceModel>()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): LogViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_log_device_view, p0, false)

        return LogViewHolder(view)
    }

    override fun getItemCount(): Int = listLogs.size

    override fun onBindViewHolder(p0: LogViewHolder, postition: Int) {
        p0.display(listLogs[postition])
    }

    /**
     * Update new Device Log and sort by TimeStamp.
     */
    fun updateLog(log: LogDeviceModel) {
        val logExits = listLogs.firstOrNull { logIndex -> logIndex.timeStamp == log.timeStamp }
        if (logExits != null) return

        listLogs.add(log)
        listLogs.sortByDescending { it.timeStamp }
        notifyDataSetChanged()
    }

    /**
     * Remove a Device Log.
     */
    fun removeLog(log: LogDeviceModel) {
        val logExits = listLogs.firstOrNull { logIndex -> logIndex.timeStamp == log.timeStamp } ?: return
        listLogs.remove(logExits)
        notifyDataSetChanged()
    }

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lblTime: AppCompatTextView = itemView.findViewById(R.id.lblTime)
        val lblSource: AppCompatTextView = itemView.findViewById(R.id.lblSource)
        val lblStatus: AppCompatTextView = itemView.findViewById(R.id.lblStatus)

        /**
         * Display Device Log.
         */
        fun display(model: LogDeviceModel) {
            lblTime.text = model.dateString()
            lblSource.text = model.sourceControl
            lblStatus.text = if (model.status) itemView.context.getString(R.string.lbl_on) else itemView.context.getString(R.string.lbl_off)
        }
    }
}