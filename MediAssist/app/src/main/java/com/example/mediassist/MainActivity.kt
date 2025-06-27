package com.example.mediassist

import android.Manifest
import android.app.AlarmManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.widget.ImageView
import com.example.mediassist.ui.ViewDoctorAvailability
import com.example.mediassist.util.EditProfileActivity
import android.content.Context
import android.provider.Settings
import com.example.mediassist.ui.ReminderListActivity
import com.example.mediassist.ui.PatientAppointmentsActivity

class MainActivity : AppCompatActivity() {

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        createNotificationChannel()

        // Profile icon click listener
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Permission check (ONLY ONCE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        val bookAppointmentBtn = findViewById<Button>(R.id.bookAppointmentButton)
        val setRemindersBtn = findViewById<Button>(R.id.setRemindersButton)
        val trackHistoryBtn = findViewById<Button>(R.id.trackMedicationHistoryButton)
        val viewAppointmentsBtn = findViewById<Button>(R.id.btnViewAppointments)

        setRemindersBtn.setOnClickListener {
            val intent = Intent(this, ReminderListActivity::class.java)
            startActivity(intent)
        }

        bookAppointmentBtn.setOnClickListener {
            val intent = Intent(this, ViewDoctorAvailability::class.java)
            startActivity(intent)
        }

        trackHistoryBtn.setOnClickListener {
            val intent = Intent(this, com.example.mediassist.ui.MedicationHistoryActivity::class.java)
            startActivity(intent)
        }

        viewAppointmentsBtn.setOnClickListener {
            val intent = Intent(this, PatientAppointmentsActivity::class.java)
            startActivity(intent)
        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "med_channel"
            val channelName = "Medication Reminders"
            val channelDescription = "Channel for medication reminder notifications"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {

                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}