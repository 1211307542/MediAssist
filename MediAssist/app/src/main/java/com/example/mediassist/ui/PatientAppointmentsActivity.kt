package com.example.mediassist.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.R
import com.example.mediassist.data.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mediassist.AppointmentAdapter

class PatientAppointmentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppointmentAdapter
    private val appointments = mutableListOf<Appointment>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val patientId: String get() = auth.currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_appointments)

        val currentUser = auth.currentUser
        val backButton = findViewById<android.widget.ImageView>(R.id.backButton)
        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Verify role is 'user'
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    
                    if (role == "healthcare" || role == "healthcare professional") {
                        Toast.makeText(this, "Only patients can view appointments", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        setupRecyclerView()
                        loadApprovedAppointments()
                    }
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to verify user role", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rvPatientAppointments)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AppointmentAdapter(appointments) { appointment ->
            showAppointmentDetailsDialog(appointment)
        }
        recyclerView.adapter = adapter
    }

    private fun showAppointmentDetailsDialog(appointment: Appointment) {
        val builder = android.app.AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_appointment_details, null)
        builder.setView(dialogView)

        val tvDoctorName = dialogView.findViewById<android.widget.TextView>(R.id.tvDoctorName)
        val tvDate = dialogView.findViewById<android.widget.TextView>(R.id.tvDate)
        val tvTime = dialogView.findViewById<android.widget.TextView>(R.id.tvTime)
        val tvReason = dialogView.findViewById<android.widget.TextView>(R.id.tvReason)
        val tvStatus = dialogView.findViewById<android.widget.TextView>(R.id.tvStatus)
        val tvDeclineReason = dialogView.findViewById<android.widget.TextView>(R.id.tvDeclineReason)

        tvDoctorName.text = "Dr. ${appointment.doctorName}"
        tvDate.text = "Date: ${appointment.date}"
        tvTime.text = "Time: ${appointment.time}"
        tvReason.text = "Reason: ${appointment.reason}"
        tvStatus.text = "Status: ${appointment.status}"

        // Fetch decline reason if declined
        if (appointment.status == "declined") {
            // Fetch from appointment_requests collection
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("appointment_requests")
                .whereEqualTo("patientId", appointment.patientId)
                .whereEqualTo("doctorId", appointment.doctorId)
                .whereEqualTo("date", appointment.date)
                .whereEqualTo("startTime", appointment.time.split(" - ")[0])
                .whereEqualTo("endTime", appointment.time.split(" - ")[1])
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val doc = result.documents[0]
                        val declineReason = doc.getString("declineReason")
                        if (!declineReason.isNullOrEmpty()) {
                            tvDeclineReason.text = "Decline Reason: $declineReason"
                            tvDeclineReason.visibility = android.view.View.VISIBLE
                        } else {
                            tvDeclineReason.visibility = android.view.View.GONE
                        }
                    } else {
                        tvDeclineReason.visibility = android.view.View.GONE
                    }
                }
        } else {
            tvDeclineReason.visibility = android.view.View.GONE
        }

        builder.setPositiveButton("OK", null)
        builder.create().show()
    }

    private fun loadApprovedAppointments() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        db.collection("appointments")
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { result ->
                appointments.clear()
                for (doc in result) {
                    val appt = doc.toObject(Appointment::class.java)
                    val appointmentDate = appt.date
                    if (appt != null && appointmentDate >= today) {
                        val appointmentWithId = appt.copy(appointmentId = doc.id)
                        appointments.add(appointmentWithId)
                    } else if (appt == null) {
                        val manualAppt = Appointment(
                            appointmentId = doc.id,
                            patientId = doc.getString("patientId") ?: "",
                            doctorId = doc.getString("doctorId") ?: "",
                            doctorName = doc.getString("doctorName") ?: "",
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            reason = doc.getString("reason") ?: "",
                            status = doc.getString("status") ?: ""
                        )
                        if (manualAppt.date >= today) {
                            appointments.add(manualAppt)
                        }
                    }
                }
                Toast.makeText(this, "Loaded ${appointments.size} appointments", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load appointments: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
