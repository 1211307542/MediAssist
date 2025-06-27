package com.example.mediassist.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.content.Intent
import android.widget.ImageView
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mediassist.R
import com.example.mediassist.data.model.Appointment
import com.example.mediassist.util.EditProfileActivity
import com.google.firebase.firestore.FirebaseFirestore

class DoctorAppointmentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoctorAppointmentsScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAppointmentsScreen() {
    val db = FirebaseFirestore.getInstance()
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }

    // Load appointments once when the screen is composed
    LaunchedEffect(Unit) {
        db.collection("appointments")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { result ->
                appointments = result.documents.mapNotNull { it.toObject(Appointment::class.java) }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pending Appointments") })
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(appointments) { appointment ->
                AppointmentCard(appointment)
            }
        }
    }
}

@Composable
fun AppointmentCard(appointment: Appointment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Patient ID: ${appointment.patientId}")
            Text("Doctor ID: ${appointment.doctorId}")
            Text("Date: ${appointment.date}")
            Text("Time: ${appointment.time}")
            Text("Reason: ${appointment.reason}")
        }
    }
}
