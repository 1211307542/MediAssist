package com.example.mediassist.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mediassist.data.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarView() {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val allAppointments = remember { mutableStateListOf<Appointment>() }

    var selectedDate by remember { mutableStateOf(currentDate()) }

    // Fetch appointments for current user
    LaunchedEffect(Unit) {
        userId?.let {
            db.collection("appointments")
                .whereEqualTo("userId", it)
                .get()
                .addOnSuccessListener { result ->
                    allAppointments.clear()
                    allAppointments.addAll(result.documents.mapNotNull { it.toObject(Appointment::class.java) })
                }
        }
    }

    val appointmentsForSelectedDate = allAppointments.filter {
        it.date == selectedDate
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select a Date", style = MaterialTheme.typography.titleLarge)

        DateSelector(selectedDate) { selectedDate = it }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Appointments for $selectedDate", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            if (appointmentsForSelectedDate.isEmpty()) {
                item { Text("No appointments") }
            } else {
                items(appointmentsForSelectedDate) { appointment ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Time: ${appointment.time}")
                            Text("Doctor ID: ${appointment.doctorId}")
                            Text("Reason: ${appointment.reason}")
                            Text("Status: ${appointment.status}")
                        }
                    }
                }
            }
        }
    }
}

// Utility Composable for Date Selection
@Composable
fun DateSelector(current: String, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    calendar.time = formatter.parse(current) ?: Date()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            onDateSelected(formatter.format(calendar.time))
        }) {
            Text("<")
        }

        Spacer(modifier = Modifier.width(16.dp))
        Text(current)
        Spacer(modifier = Modifier.width(16.dp))

        Button(onClick = {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            onDateSelected(formatter.format(calendar.time))
        }) {
            Text(">")
        }
    }
}

// Utility
fun currentDate(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date())
}
