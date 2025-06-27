package com.example.mediassist.data.model

data class AvailabilitySlot(
    val doctorId: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val isBooked: Boolean = false,
    val slotId: String = ""
)
