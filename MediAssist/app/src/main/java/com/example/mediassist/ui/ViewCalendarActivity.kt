package com.example.mediassist.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.R
import com.example.mediassist.data.model.AppointmentRequest
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class ViewCalendarActivity : AppCompatActivity() {

    private lateinit var compactCalendarView: CompactCalendarView
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvMonth: TextView
    private lateinit var btnPrevMonth: ImageView
    private lateinit var btnNextMonth: ImageView
    private lateinit var rvAppointments: RecyclerView
    private lateinit var appointmentAdapter: AppointmentListAdapter
    private lateinit var btnSyncCalendar: Button
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var mCalendar: Calendar? = null
    private var userRole: String? = null

    private val appointmentList = mutableListOf<AppointmentRequest>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dateFormatMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dateFormatDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_calendar)


        // UI elements
        compactCalendarView = findViewById(R.id.compactcalendar_view)
        tvSelectedDate = findViewById(R.id.tv_selected_date)
        tvMonth = findViewById(R.id.tv_month)
        btnPrevMonth = findViewById(R.id.btn_prev_month)
        btnNextMonth = findViewById(R.id.btn_next_month)
        rvAppointments = findViewById(R.id.rv_appointments)
        btnSyncCalendar = findViewById(R.id.btn_sync_calendar)
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        appointmentAdapter = AppointmentListAdapter(appointmentList)
        rvAppointments.layoutManager = LinearLayoutManager(this)
        rvAppointments.adapter = appointmentAdapter

        // Set month name
        tvMonth.text = dateFormatMonth.format(compactCalendarView.firstDayOfCurrentMonth)

        // Month navigation
        btnPrevMonth.setOnClickListener {
            compactCalendarView.scrollLeft()
            tvMonth.text = dateFormatMonth.format(compactCalendarView.firstDayOfCurrentMonth)
        }

        btnNextMonth.setOnClickListener {
            compactCalendarView.scrollRight()
            tvMonth.text = dateFormatMonth.format(compactCalendarView.firstDayOfCurrentMonth)
        }

        btnSyncCalendar.setOnClickListener {
            requestCalendarAccess()
        }

        compactCalendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateClicked)
                tvSelectedDate.text = "Appointments on: $selectedDate"
                loadAppointmentsForDate(selectedDate)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                tvMonth.text = dateFormatMonth.format(firstDayOfNewMonth)
            }
        })

        // Load appointments for today
        val today = dateFormatDay.format(Date())
        tvSelectedDate.text = "Appointments on: $today"
        loadAppointmentsForDate(today)
        loadAppointmentDots()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR))
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        fetchUserRole()

        // Always start fresh - don't check for existing signed-in user
        // This ensures user can select account every time
    }

    private fun fetchUserRole() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                userRole = document.getString("role")
                // Load initial data after getting role
                val today = dateFormatDay.format(Date())
                tvSelectedDate.text = "Appointments on: $today"
                loadAppointmentsForDate(today)
                loadAppointmentDots()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch user role: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestCalendarAccess() {
        try {
            // Sign out first to ensure user gets account selection prompt
            mGoogleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = mGoogleSignInClient.signInIntent
                signInLauncher.launch(signInIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                updateUIWithCalendarService(account)
                syncAllFutureAppointmentsToCalendar() // Sync after successful sign-in
            } catch (e: ApiException) {
                when (e.statusCode) {
                    12501 -> {
                        Toast.makeText(this, "Sign-in cancelled. To sync your calendar, please grant access.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this, "Google Sign-In failed with error code: ${e.statusCode}. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            // User cancelled the sign-in flow.
            Toast.makeText(this, "Sign-in process was cancelled. Please try again to sync your calendar.", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUIWithCalendarService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            this, Collections.singleton(CalendarScopes.CALENDAR)
        )
        credential.selectedAccount = account.account
        mCalendar = Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(getString(R.string.app_name)).build()

        Toast.makeText(this, "Calendar access granted", Toast.LENGTH_SHORT).show()
    }

    private fun syncAllFutureAppointmentsToCalendar() {
        if (mCalendar == null) {
            Toast.makeText(this, "Please grant calendar access first.", Toast.LENGTH_SHORT).show()
            requestCalendarAccess()
            return
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val today = dateFormatDay.format(Date())

        db.collection("appointment_requests")
            .whereEqualTo("doctorId", currentUserId)
            .whereEqualTo("status", "approved")
            .whereGreaterThanOrEqualTo("date", today)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "No future appointments to sync.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val futureAppointments = result.toObjects(AppointmentRequest::class.java)

                CoroutineScope(Dispatchers.IO).launch {
                    var syncedCount = 0
                    var skippedCount = 0
                    try {
                        for (appointment in futureAppointments) {
                            val startDateTimeStr = "${appointment.date}T${appointment.startTime}:00"
                            val endDateTimeStr = "${appointment.date}T${appointment.endTime}:00"

                            val startDateTime = com.google.api.client.util.DateTime(startDateTimeStr)
                            val endDateTime = com.google.api.client.util.DateTime(endDateTimeStr)

                            // Check if event already exists to avoid duplicates
                            val existingEvents = mCalendar?.events()?.list("primary")
                                ?.setTimeMin(startDateTime)
                                ?.setTimeMax(endDateTime)
                                ?.setQ("Appointment with ${appointment.patientName}")
                                ?.execute()

                            if (existingEvents != null && existingEvents.items.any { it.summary == "Appointment with ${appointment.patientName}" }) {
                                skippedCount++
                                continue
                            }

                            val event = com.google.api.services.calendar.model.Event().apply {
                                summary = "Appointment with ${appointment.patientName}"
                                description = "Appointment for ${appointment.patientName} with Dr. ${appointment.doctorName}.\nReason: ${appointment.reason}"
                                val timeZone = java.util.TimeZone.getDefault().id

                                start = com.google.api.services.calendar.model.EventDateTime().setDateTime(startDateTime).setTimeZone(timeZone)
                                end = com.google.api.services.calendar.model.EventDateTime().setDateTime(endDateTime).setTimeZone(timeZone)
                            }
                            mCalendar?.events()?.insert("primary", event)?.execute()
                            syncedCount++
                        }
                        runOnUiThread {
                            val message = when {
                                syncedCount > 0 && skippedCount > 0 -> "$syncedCount new appointments synced, $skippedCount already existed."
                                syncedCount > 0 -> "$syncedCount new appointments synced successfully."
                                skippedCount > 0 -> "All future appointments already exist in your calendar."
                                else -> "No new appointments to sync."
                            }
                            Toast.makeText(this@ViewCalendarActivity, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this@ViewCalendarActivity, "An error occurred while syncing appointments: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load future appointments: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadAppointmentsForDate(date: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("appointment_requests")
            .whereEqualTo("doctorId", currentUserId)
            .whereEqualTo("status", "approved")
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { result ->
                appointmentList.clear()
                for (doc in result) {
                    val appt = doc.toObject(AppointmentRequest::class.java)
                    appointmentList.add(appt)
                }
                appointmentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load appointments: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadAppointmentDots() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("appointment_requests")
            .whereEqualTo("doctorId", currentUserId)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { result ->
                val eventDates = mutableSetOf<Long>()
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                for (doc in result) {
                    val appt = doc.toObject(AppointmentRequest::class.java)
                    val parsedDate = formatter.parse(appt.date)
                    parsedDate?.let {
                        eventDates.add(it.time)
                    }
                }
                // Add event dots
                for (time in eventDates) {
                    val event = Event(Color.parseColor("#FF4081"), time)
                    compactCalendarView.addEvent(event)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load event dots: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
