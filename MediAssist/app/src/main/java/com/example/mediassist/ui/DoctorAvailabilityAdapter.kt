package com.example.mediassist.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.R
import com.example.mediassist.data.model.AvailabilitySlot
import android.util.Log

class DoctorAvailabilityAdapter(
    private val slots: List<ViewDoctorAvailability.AvailabilitySlotWithDoctor>,
    private val onBook: (ViewDoctorAvailability.AvailabilitySlotWithDoctor) -> Unit
) : RecyclerView.Adapter<DoctorAvailabilityAdapter.SlotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_availability, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slotWithDoctor = slots[position]
        Log.d("DoctorAvailabilityAdapter", "Binding slot: ${slotWithDoctor.doctorName} - ${slotWithDoctor.slot.startTime}")
        holder.bind(slotWithDoctor)
    }

    override fun getItemCount(): Int {
        Log.d("DoctorAvailabilityAdapter", "Item count: ${slots.size}")
        return slots.size
    }

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDoctorName: TextView = itemView.findViewById(R.id.doctorName)
        private val tvSlotDate: TextView = itemView.findViewById(R.id.slotDate)
        private val tvSlotTime: TextView = itemView.findViewById(R.id.slotTime)
        private val btnBook: Button = itemView.findViewById(R.id.btnBook)

        fun bind(slotWithDoctor: ViewDoctorAvailability.AvailabilitySlotWithDoctor) {
            tvDoctorName.text = "Dr. ${slotWithDoctor.doctorName}"
            tvSlotDate.text = slotWithDoctor.slot.date
            tvSlotTime.text = "${slotWithDoctor.slot.startTime} - ${slotWithDoctor.slot.endTime}"
            btnBook.setOnClickListener { onBook(slotWithDoctor) }
        }
    }
}