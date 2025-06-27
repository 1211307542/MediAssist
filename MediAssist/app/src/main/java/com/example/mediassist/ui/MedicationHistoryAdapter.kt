package com.example.mediassist.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.R
import com.example.mediassist.data.model.MedicationHistory

class MedicationHistoryAdapter(private val list: List<MedicationHistory>) :
    RecyclerView.Adapter<MedicationHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicationName: TextView = itemView.findViewById(R.id.textMedicationName)
        val dosageAmount: TextView = itemView.findViewById(R.id.textDosageAmount)
        val time: TextView = itemView.findViewById(R.id.textTime)
        val remainingDays: TextView = itemView.findViewById(R.id.textRemainingDays)
        val status: TextView = itemView.findViewById(R.id.textStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.medication_history_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.medicationName.text = item.medicationName
        holder.dosageAmount.text = "Dosage: ${item.dosageAmount}"
        holder.time.text = "Time: ${item.getReadableTime()}"

        val remaining = item.getRemainingDays()
        holder.remainingDays.text = if (remaining > 0) {
            "Remaining: $remaining days"
        } else {
            "Completed"
        }

        holder.status.text = "Status: ${item.getStatus()}"
    }
}
