package com.example.mediassist.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.R
import com.example.mediassist.data.model.AvailabilitySlot

class AvailabilitySlotAdapter(
    private val slots: List<AvailabilitySlot>,
    private val onEdit: (AvailabilitySlot) -> Unit,
    private val onDelete: (AvailabilitySlot) -> Unit
) : RecyclerView.Adapter<AvailabilitySlotAdapter.SlotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_availability_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slot = slots[position]
        holder.bind(slot)
    }

    override fun getItemCount() = slots.size

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSlot: TextView = itemView.findViewById(R.id.tvSlot)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(slot: AvailabilitySlot) {
            tvSlot.text = "${slot.date}, ${slot.startTime} - ${slot.endTime}"
            btnEdit.setOnClickListener { onEdit(slot) }
            btnDelete.setOnClickListener { onDelete(slot) }
        }
    }
}