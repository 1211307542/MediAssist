package com.example.mediassist.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.R
import com.example.mediassist.data.model.AppointmentRequest

class AppointmentListAdapter(
    private val appointments: MutableList<AppointmentRequest>
) : RecyclerView.Adapter<AppointmentListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPatientName: TextView = view.findViewById(R.id.tv_patient_name)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val tvReason: TextView = view.findViewById(R.id.tv_reason)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = appointments.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.tvPatientName.text = "Patient: ${appointment.patientName}"
        holder.tvTime.text = "Time: ${appointment.startTime} - ${appointment.endTime}"
        holder.tvReason.text = "Reason: ${appointment.reason}"
    }
}
