package com.example.mediassist

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.mediassist.data.model.AppointmentRequest
import android.widget.ProgressBar
import android.content.Intent

class AppointmentRequestsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: AppointmentRequestAdapter
    private val appointments = mutableListOf<AppointmentRequest>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_requests)

        recyclerView = findViewById(R.id.recycler_appointment_requests)
        progressBar = findViewById(R.id.progressBar_requests)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AppointmentRequestAdapter(appointments) { appointment, action ->
            handleAppointmentAction(appointment, action)
        }

        recyclerView.adapter = adapter

        loadAppointmentRequests()

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadAppointmentRequests() {
        progressBar.visibility = View.VISIBLE

        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        db.collection("appointment_requests")
            .whereEqualTo("doctorId", currentUserId) // <-- FILTER by doctorId
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                appointments.clear()
                for (doc in documents) {
                    val request = doc.toObject(AppointmentRequest::class.java)
                    request.id = doc.id
                    if (request.status == "pending") {
                        appointments.add(request)
                    }
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load appointment requests", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }

    private fun handleAppointmentAction(appointment: AppointmentRequest, action: String) {
        val docRef = db.collection("appointment_requests").document(appointment.id)

        docRef.update("status", action)
            .addOnSuccessListener {
                Toast.makeText(this, "Appointment $action", Toast.LENGTH_SHORT).show()
                appointments.remove(appointment)
                adapter.notifyDataSetChanged()

                if (action == "approved") {
                    saveApprovedAppointmentToAppointmentsCollection(appointment)
                    val intent = Intent(this, com.example.mediassist.ui.ViewCalendarActivity::class.java)
                    startActivity(intent)
                } else if (action == "declined") {
                    // Mark the availability slot as booked when declined so it's no longer available
                    markAvailabilitySlotAsBooked(appointment)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveApprovedAppointmentToAppointmentsCollection(appointment: AppointmentRequest) {
        val appointmentData = hashMapOf(
            "patientId" to appointment.patientId,
            "doctorId" to appointment.doctorId,
            "doctorName" to appointment.doctorName,
            "doctorEmail" to appointment.doctorEmail,
            "date" to appointment.date,
            "time" to "${appointment.startTime} - ${appointment.endTime}",
            "reason" to appointment.reason,
            "status" to "approved"
        )

        FirebaseFirestore.getInstance().collection("appointments")
            .add(appointmentData)
            .addOnSuccessListener { docRef ->
                docRef.update("appointmentId", docRef.id)
                    .addOnSuccessListener {
                        // Mark the availability slot as booked
                        markAvailabilitySlotAsBooked(appointment)
                        Toast.makeText(this, "Appointment saved successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to update appointment ID: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save approved appointment: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun markAvailabilitySlotAsBooked(appointment: AppointmentRequest) {
        // Find and update the corresponding availability slot
        db.collection("doctor_availability")
            .whereEqualTo("doctorId", appointment.doctorId)
            .whereEqualTo("date", appointment.date)
            .whereEqualTo("startTime", appointment.startTime)
            .whereEqualTo("endTime", appointment.endTime)
            .whereEqualTo("isBooked", false)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val slotDoc = result.documents[0]
                    slotDoc.reference.update("isBooked", true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Time slot marked as booked", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Failed to mark slot as booked: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Could not find the availability slot to mark as booked", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to find availability slot: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}




