package com.example.mediassist
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.R
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.View
import com.example.mediassist.data.model.Reminder
import com.example.mediassist.data.model.ReminderTime

class ReminderAdapter(
    private val context: Context,
    private val reminders: MutableList<Reminder>,
    private val reminderTimesMap: Map<Int, List<ReminderTime>>,
    private val onUpdate: (Reminder) -> Unit,
    private val onDelete: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMedicine: TextView = view.findViewById(R.id.tvMedicine)
        val tvDosage: TextView = view.findViewById(R.id.tvDosage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvStartDate: TextView = view.findViewById(R.id.tvStartDate)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val btnUpdate: Button = view.findViewById(R.id.btnUpdate)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]

        val timesList = reminderTimesMap[reminder.id] ?: emptyList()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTimes = timesList.joinToString(", ") { rt ->
            timeFormat.format(Date(rt.timeInMillis))
        }
        holder.tvMedicine.text = "Medicine: ${reminder.medicationName}"
        holder.tvDosage.text = "Dosage: ${reminder.dosageAmount}"
        holder.tvTime.text = "Time(s): $formattedTimes"
        holder.tvStartDate.text = "Start: ${reminder.startDate}"
        holder.tvDuration.text = "Duration: ${reminder.duration} days"

        holder.btnUpdate.setOnClickListener { onUpdate(reminder) }
        holder.btnDelete.setOnClickListener { onDelete(reminder) }
    }

    override fun getItemCount(): Int = reminders.size
}
