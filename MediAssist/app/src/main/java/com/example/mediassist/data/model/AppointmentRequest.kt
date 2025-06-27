package com.example.mediassist.data.model

import com.google.firebase.Timestamp

data class AppointmentRequest(
    var id: String = "",
    val patientName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorEmail: String = "",
    val patientId: String = "",
    val slotId: String = "",
    val reason: String = "",
    val status: String = "",
    val timestamp: Timestamp? = null,
    val date: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

