package com.example.mediassist.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.R
import com.example.mediassist.data.model.AvailabilitySlot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.content.Intent
import android.widget.ImageView
import com.example.mediassist.WelcomeActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.view.View
import android.widget.CheckBox
import com.example.mediassist.util.EditProfileActivity

class ManageAvailabilityActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AvailabilitySlotAdapter
    private val slots = mutableListOf<AvailabilitySlot>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val doctorId: String get() = auth.currentUser?.uid ?: "doctor123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_availability)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        verifyAuthentication()

        recyclerView = findViewById(R.id.rvSlots)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AvailabilitySlotAdapter(slots, this::editSlot, this::deleteSlot)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnAddSlot).setOnClickListener {
            showSlotDialog()
        }

        migrateAllSlotsToIsBooked()
        loadSlots()
    }

    private fun verifyAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to manage availability", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role != "healthcare" && role != "healthcare professional") {
                        Toast.makeText(this, "Only doctors can manage availability", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to verify user role", Toast.LENGTH_SHORT).show()
            }
    }

    private fun migrateAllSlotsToIsBooked() {
        db.collection("doctor_availability")
            .whereEqualTo("doctorId", doctorId)
            .get()
            .addOnSuccessListener { result ->
                val batch = db.batch()
                var hasChanges = false

                for (doc in result) {
                    val data = doc.data
                    if (data.containsKey("booked") && !data.containsKey("isBooked")) {
                        val isBooked = data["booked"] as? Boolean ?: false
                        batch.update(doc.reference, mapOf(
                            "isBooked" to isBooked,
                            "booked" to com.google.firebase.firestore.FieldValue.delete()
                        ))
                        hasChanges = true
                    }
                }

                if (hasChanges) {
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("ManageAvailability", "Successfully migrated all old slots")
                            loadSlots()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ManageAvailability", "Failed to migrate slots: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ManageAvailability", "Failed to get slots for migration: ${e.message}")
            }
    }

    private fun loadSlots() {
        db.collection("doctor_availability")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("isBooked", false)
            .get()
            .addOnSuccessListener { result ->
                slots.clear()
                for (doc in result) {
                    val slot = doc.toObject(AvailabilitySlot::class.java).copy(slotId = doc.id)
                    slots.add(slot)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun showSlotDialog(slot: AvailabilitySlot? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_slot, null)
        val etDate = dialogView.findViewById<EditText>(R.id.etDate)
        val etStart = dialogView.findViewById<EditText>(R.id.etStartTime)
        val etEnd = dialogView.findViewById<EditText>(R.id.etEndTime)
        val btnAddTimeSlot = dialogView.findViewById<Button>(R.id.btnAddTimeSlot)
        val cbRecurring = dialogView.findViewById<CheckBox>(R.id.cbRecurring)
        val etRepeatWeeks = dialogView.findViewById<EditText>(R.id.etRepeatWeeks)

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        if (slot != null) {
            etDate.setText(slot.date)
            etStart.setText(slot.startTime)
            etEnd.setText(slot.endTime)
            btnAddTimeSlot.text = "Update Time Slot"
        }

        etDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                val selectedDate = Calendar.getInstance().apply { set(y, m, d) }
                val today = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                if (selectedDate.before(today)) {
                    Toast.makeText(this, "Cannot select past dates", Toast.LENGTH_SHORT).show()
                } else {
                    etDate.setText(dateFormat.format(calendar.time))
                }
            }, year, month, day).show()
        }

        etStart.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, h, m ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, m)
                etStart.setText(timeFormat.format(calendar.time))
            }, hour, minute, true).show()
        }

        etEnd.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, h, m ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, m)
                etEnd.setText(timeFormat.format(calendar.time))
            }, hour, minute, true).show()
        }

        cbRecurring.setOnCheckedChangeListener { _, isChecked ->
            etRepeatWeeks.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (slot == null) "Add Slot" else "Edit Slot")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        btnAddTimeSlot.setOnClickListener {
            val date = etDate.text.toString().trim()
            val start = etStart.text.toString().trim()
            val end = etEnd.text.toString().trim()
            val isRecurring = cbRecurring.isChecked
            val repeatWeeks = etRepeatWeeks.text.toString().trim().toIntOrNull() ?: 0

            if (date.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isRecurring && repeatWeeks > 0) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val initialDate = Calendar.getInstance().apply { time = sdf.parse(date)!! }

                for (i in 0 until repeatWeeks) {
                    val currentDate = Calendar.getInstance().apply { time = initialDate.time }
                    currentDate.add(Calendar.WEEK_OF_YEAR, i)
                    val newDate = sdf.format(currentDate.time)
                    addSlot(newDate, start, end)
                }
            } else {
                if (slot == null) addSlot(date, start, end)
                else updateSlot(slot.slotId, date, start, end)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addSlot(date: String, start: String, end: String) {
        // Check for duplicate slots first
        checkForDuplicateSlot(date, start, end) { isDuplicate ->
            if (isDuplicate) {
                Toast.makeText(this, "A slot already exists for this date and time", Toast.LENGTH_SHORT).show()
                return@checkForDuplicateSlot
            }
            
            val slot = AvailabilitySlot(
                doctorId = doctorId,
                date = date,
                startTime = start,
                endTime = end,
                isBooked = false
            )

            db.collection("doctor_availability")
                .add(slot)
                .addOnSuccessListener {
                    Toast.makeText(this, "Slot added", Toast.LENGTH_SHORT).show()
                    loadSlots()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add slot", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun checkForDuplicateSlot(date: String, start: String, end: String, callback: (Boolean) -> Unit) {
        db.collection("doctor_availability")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("date", date)
            .whereEqualTo("startTime", start)
            .whereEqualTo("endTime", end)
            .get()
            .addOnSuccessListener { result ->
                callback(!result.isEmpty)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to check for duplicates", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    private fun updateSlot(slotId: String, date: String, start: String, end: String) {
        db.collection("doctor_availability").document(slotId)
            .get()
            .addOnSuccessListener { document ->
                val currentSlot = document.toObject(AvailabilitySlot::class.java)
                val isBooked = currentSlot?.isBooked ?: false

                db.collection("doctor_availability").document(slotId)
                    .update(mapOf(
                        "date" to date,
                        "startTime" to start,
                        "endTime" to end,
                        "isBooked" to isBooked
                    ))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Slot updated", Toast.LENGTH_SHORT).show()
                        loadSlots()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update slot", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch slot", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteSlot(slot: AvailabilitySlot) {
        db.collection("doctor_availability").document(slot.slotId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Slot deleted", Toast.LENGTH_SHORT).show()
                loadSlots()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete slot", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editSlot(slot: AvailabilitySlot) {
        showSlotDialog(slot)
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
