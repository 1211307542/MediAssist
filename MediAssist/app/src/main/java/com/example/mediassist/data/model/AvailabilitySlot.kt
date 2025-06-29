package com.example.mediassist.data.model

data class AvailabilitySlot(
    var doctorId: String = "",
    var date: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var isBooked: Boolean = false,
    var slotId: String = ""
)
