package com.example.mediassist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.data.model.Appointment

class AppointmentAdapter(private val appointments: List<Appointment>, private val onItemClick: (Appointment) -> Unit) :
    RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDoctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val tvReason: TextView = itemView.findViewById(R.id.tvReason)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment_card, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.tvDoctorName.text = "Dr. ${appointment.doctorName}"
        holder.tvDateTime.text = "Date: ${appointment.date}\nTime: ${appointment.time}"
        holder.tvReason.text = "Reason: ${appointment.reason}"
        holder.itemView.setOnClickListener { onItemClick(appointment) }
    }

    override fun getItemCount(): Int = appointments.size
}

