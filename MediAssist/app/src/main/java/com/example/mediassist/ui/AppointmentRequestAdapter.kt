package com.example.mediassist

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.data.model.AppointmentRequest
import com.example.mediassist.ui.ViewCalendarActivity
import com.google.firebase.firestore.FirebaseFirestore

class AppointmentRequestAdapter(
    private val appointmentRequests: List<AppointmentRequest>,
    private val onActionClick: (AppointmentRequest, String) -> Unit
) : RecyclerView.Adapter<AppointmentRequestAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPatientName: TextView = itemView.findViewById(R.id.tv_patient_name)
        val tvAppointmentDateTime: TextView = itemView.findViewById(R.id.tv_appointment_datetime)
        val tvReason: TextView = itemView.findViewById(R.id.tv_reason)
        val btnApprove: Button = itemView.findViewById(R.id.btn_approve)
        val btnDecline: Button = itemView.findViewById(R.id.btn_decline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = appointmentRequests[position]

        holder.tvPatientName.text = request.patientName
        holder.tvAppointmentDateTime.text = "Date: ${request.date}  Time: ${request.startTime} - ${request.endTime}"
        holder.tvReason.text = "Reason: ${request.reason}"

        holder.btnApprove.setOnClickListener {
            onActionClick(request, "approved")
        }

        holder.btnDecline.setOnClickListener {
            onActionClick(request, "declined")
        }
    }

    override fun getItemCount(): Int = appointmentRequests.size

}
