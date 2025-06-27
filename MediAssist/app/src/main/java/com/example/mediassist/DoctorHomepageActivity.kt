package com.example.mediassist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.widget.Button
import android.widget.ImageView
import com.example.mediassist.ui.ManageAvailabilityActivity
import com.example.mediassist.util.EditProfileActivity
import com.example.mediassist.ui.ViewCalendarActivity

class DoctorHomepageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_homepage)

        // Profile icon click listener
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val welcomeTextView = findViewById<TextView>(R.id.textViewWelcomeDoctor)
        val manageAvailabilityButton = findViewById<Button>(R.id.manageAvailabilityButton)
        val btnViewCalendar = findViewById<Button>(R.id.btn_view_calendar)

        manageAvailabilityButton.setOnClickListener {
            val intent = Intent(this, ManageAvailabilityActivity::class.java)
            startActivity(intent)
        }

        btnViewCalendar.setOnClickListener {
            val intent = Intent(this, ViewCalendarActivity::class.java)
            startActivity(intent)
        }

        val viewRequestsButton = findViewById<Button>(R.id.viewAppointmentRequestsButton)
        viewRequestsButton.setOnClickListener {
            val intent = Intent(this, AppointmentRequestsActivity::class.java)
            startActivity(intent)
        }

        // Fetch the current user's full name from Firestore
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName")
                        welcomeTextView.text = "Welcome Dr. $fullName"
                    }
                }
                .addOnFailureListener {
                    welcomeTextView.text = "Welcome Doctor"
                }
        }
    }
}
