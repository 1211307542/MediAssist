package com.example.mediassist

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AlarmRingingActivity : AppCompatActivity() {

    private lateinit var alarmTimeTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_ringing)

        val medicationName = intent.getStringExtra("medicationName") ?: "No name"
        val dosageAmount = intent.getStringExtra("dosageAmount") ?: "No dosage"
        val reminderTime = intent.getStringExtra("reminderTime")

        findViewById<TextView>(R.id.medicationName).text = "Medication: $medicationName"
        findViewById<TextView>(R.id.medicationDosage).text = "Dosage: $dosageAmount"
        findViewById<TextView>(R.id.medicationInstruction).text = ""

        alarmTimeTextView = findViewById(R.id.alarmTime)
        if (reminderTime != null) {
            alarmTimeTextView.text = reminderTime
        } else {
            // fallback to current time if not provided
            updateTime()
            handler.post(updateTimeRunnable)
        }

        // Dismiss button closes the activity
        findViewById<Button>(R.id.dismissButton).setOnClickListener {
            finish()
        }
    }

    private fun updateTime() {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeString = dateFormat.format(currentTime)

        alarmTimeTextView.text = timeString
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable) // Stop updating when activity is destroyed
    }
}
