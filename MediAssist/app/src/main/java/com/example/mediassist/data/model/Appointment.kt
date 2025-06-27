package com.example.mediassist.data.model

data class Appointment(
    val appointmentId: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val date: String = "",
    val time: String = "", // "09:00 AM - 11:00 AM"
    val reason: String = "",
    val status: String = ""
)
