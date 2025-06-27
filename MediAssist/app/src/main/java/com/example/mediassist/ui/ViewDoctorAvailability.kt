package com.example.mediassist.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.R
import com.example.mediassist.data.model.AvailabilitySlot
import com.example.mediassist.data.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageView
import android.util.Log
import android.content.Intent

class ViewDoctorAvailability : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DoctorAvailabilityAdapter
    private val slots = mutableListOf<AvailabilitySlotWithDoctor>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Data class to hold both slot and doctor info
    data class AvailabilitySlotWithDoctor(
        val slot: AvailabilitySlot,
        val doctorName: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_doctor_availability)
        Log.d("ViewDoctorAvailability", "Activity created")

        recyclerView = findViewById(R.id.recyclerViewAvailability)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Add this line to see if the RecyclerView is found
        if (recyclerView == null) {
            Log.e("ViewDoctorAvailability", "RecyclerView not found!")
        } else {
            Log.d("ViewDoctorAvailability", "RecyclerView found")
        }

        adapter = DoctorAvailabilityAdapter(slots) { slotWithDoctor ->
            bookAppointment(slotWithDoctor)
        }
        recyclerView.adapter = adapter

        loadAllDoctorAvailability()

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadAllDoctorAvailability() {
        Log.d("ViewDoctorAvailability", "Starting to load availability")
        db.collection("doctor_availability")
            .whereEqualTo("isBooked", false)
            .get()
            .addOnSuccessListener { result ->
                Log.d("ViewDoctorAvailability", "Query successful. Document count: ${result.documents.size}")

                // Log each document
                result.documents.forEach { doc ->
                    Log.d("ViewDoctorAvailability", "Document ID: ${doc.id}")
                    Log.d("ViewDoctorAvailability", "Document data: ${doc.data}")
                }

                val doctorIds = result.documents.mapNotNull { doc ->
                    val slot = doc.toObject(AvailabilitySlot::class.java)
                    Log.d("ViewDoctorAvailability", "Processing slot: ${slot?.startTime} - ${slot?.endTime}")
                    slot?.doctorId
                }.distinct()

                Log.d("ViewDoctorAvailability", "Found ${doctorIds.size} unique doctors: $doctorIds")

                var processedDoctors = 0
                doctorIds.forEach { doctorId ->
                    Log.d("ViewDoctorAvailability", "Fetching doctor info for ID: $doctorId")
                    db.collection("users")
                        .document(doctorId)
                        .get()
                        .addOnSuccessListener { doctorDoc ->
                            val doctorName = doctorDoc.getString("fullName") ?: "Unknown Doctor"
                            Log.d("ViewDoctorAvailability", "Got doctor name: $doctorName")

                            // Add all slots for this doctor
                            result.documents.forEach { slotDoc ->
                                val slot = slotDoc.toObject(AvailabilitySlot::class.java)?.copy(slotId = slotDoc.id)
                                if (slot?.doctorId == doctorId) {
                                    slots.add(AvailabilitySlotWithDoctor(slot, doctorName))
                                    Log.d("ViewDoctorAvailability", "Added slot: ${slot.startTime} - ${slot.endTime}")
                                }
                            }
                            processedDoctors++
                            if (processedDoctors == doctorIds.size) {
                                Log.d("ViewDoctorAvailability", "All doctors processed. Total slots: ${slots.size}")
                                adapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ViewDoctorAvailability", "Error fetching doctor: ${e.message}")
                            processedDoctors++
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewDoctorAvailability", "Error loading slots: ${e.message}")
                Toast.makeText(this, "Failed to load availability", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bookAppointment(slotWithDoctor: AvailabilitySlotWithDoctor) {
        val doctorId = slotWithDoctor.slot.doctorId
        val slotId = slotWithDoctor.slot.slotId

        if (doctorId.isNullOrEmpty() || slotId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid doctor or slot information", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ReasonForConsultationActivity::class.java).apply {
            putExtra("doctorId", doctorId)
            putExtra("slotId", slotId)
            putExtra("slotDate", slotWithDoctor.slot.date)
            putExtra("startTime", slotWithDoctor.slot.startTime)
            putExtra("endTime", slotWithDoctor.slot.endTime)
        }
        startActivity(intent)
    }
}