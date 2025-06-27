package com.example.mediassist.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mediassist.R

class ReasonForConsultationActivity : AppCompatActivity() {
    private lateinit var reasonInput: EditText
    private lateinit var okButton: Button

    private lateinit var doctorId: String
    private lateinit var slotId: String
    private lateinit var slotDate: String
    private lateinit var startTime: String
    private lateinit var endTime: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reason_for_consultation)

        doctorId = intent.getStringExtra("doctorId") ?: ""
        slotId = intent.getStringExtra("slotId") ?: ""
        slotDate = intent.getStringExtra("slotDate") ?: ""
        startTime = intent.getStringExtra("startTime") ?: ""
        endTime = intent.getStringExtra("endTime") ?: ""

        reasonInput = findViewById(R.id.et_reason)
        okButton = findViewById(R.id.btn_ok)

        okButton.setOnClickListener {
            val reason = reasonInput.text.toString().trim()
            if (reason.isEmpty()) {
                Toast.makeText(this, "Please enter a reason", Toast.LENGTH_SHORT).show()
            } else {
                checkAndBookSlot(reason)
            }
        }
    }

    private fun checkAndBookSlot(reason: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("doctor_availability").document(slotId)
            .get()
            .addOnSuccessListener { document ->
                val isBooked = document.getBoolean("isBooked") ?: false
                if (isBooked) {
                    Toast.makeText(this, "This slot is already booked. Please choose another.", Toast.LENGTH_SHORT).show()
                } else {
                    submitAppointmentRequest(reason)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to check slot: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun submitAppointmentRequest(reason: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userId = currentUser.uid

        // First get patient name
        db.collection("users").document(userId).get()
            .addOnSuccessListener { patientDocument ->
                val patientName = patientDocument.getString("fullName")
                if (patientName.isNullOrBlank()) {
                    Toast.makeText(this, "Your profile does not have a name set.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                // Then get doctor information
                db.collection("users").document(doctorId).get()
                    .addOnSuccessListener { doctorDocument ->
                        val doctorName = doctorDocument.getString("fullName") ?: "Dr. Unknown"
                        val doctorEmail = doctorDocument.getString("email") ?: ""

                        val appointmentRequest = hashMapOf(
                            "doctorId" to doctorId,
                            "doctorName" to doctorName,
                            "doctorEmail" to doctorEmail,
                            "patientId" to userId,
                            "patientName" to patientName,
                            "reason" to reason,
                            "status" to "pending",
                            "timestamp" to FieldValue.serverTimestamp(),
                            "date" to slotDate,
                            "startTime" to startTime,
                            "endTime" to endTime
                        )

                        db.collection("appointment_requests")
                            .add(appointmentRequest)
                            .addOnSuccessListener {
                                // Mark slot as booked
                                db.collection("doctor_availability").document(slotId)
                                    .update("isBooked", true)

                                Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to send request: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to fetch doctor information: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch user name: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
