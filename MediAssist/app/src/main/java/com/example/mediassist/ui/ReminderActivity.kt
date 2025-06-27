package com.example.mediassist

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mediassist.broadcast.ReminderReceiver
import com.example.mediassist.data.ReminderDatabase
import com.example.mediassist.data.model.Reminder
import com.example.mediassist.data.model.ReminderTime
import com.example.mediassist.util.EditProfileActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ReminderActivity : AppCompatActivity() {

    private lateinit var editTextMedicine: EditText
    private lateinit var editTextDosage: EditText
    private lateinit var editTextStartDate: EditText
    private lateinit var editTextDuration: EditText
    private lateinit var textViewEndDate: TextView
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button
    private lateinit var buttonAddTime: Button
    private lateinit var timeContainer: LinearLayout

    private var selectedStartDate: Calendar = Calendar.getInstance()
    private val selectedTimes: MutableList<Calendar> = mutableListOf()
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 2001
    private var existingReminderId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_reminder)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        editTextMedicine = findViewById(R.id.editTextMedicine)
        editTextDosage = findViewById(R.id.editTextDosage)
        editTextStartDate = findViewById(R.id.editTextStartDate)
        editTextDuration = findViewById(R.id.editTextDuration)
        textViewEndDate = findViewById(R.id.textViewEndDate)
        buttonSave = findViewById(R.id.buttonSave)
        buttonCancel = findViewById(R.id.buttonCancel)
        buttonAddTime = findViewById(R.id.buttonAddTime)
        timeContainer = findViewById(R.id.timeContainer)

        editTextStartDate.setOnClickListener { showDatePicker() }
        buttonAddTime.setOnClickListener { showTimePicker() }

        editTextDuration.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateEndDate() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        buttonSave.setOnClickListener { saveReminder() }
        buttonCancel.setOnClickListener { finish() }

        val profileIcon = findViewById<ImageView>(R.id.imageProfile)
        profileIcon.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // If editing an existing reminder
        val passedReminder = intent.getParcelableExtra<Reminder>("reminder")
        passedReminder?.let {
            existingReminderId = it.id
            editTextMedicine.setText(it.medicationName)
            editTextDosage.setText(it.dosageAmount)
            editTextDuration.setText(it.duration)
            editTextStartDate.setText(it.startDate)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedStartDate.time = dateFormat.parse(it.startDate) ?: Date()
        }
    }

    private fun showDatePicker() {
        val today = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            selectedStartDate.set(year, month, day)
            editTextStartDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedStartDate.time))
            updateEndDate()
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        val now = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, m)
            calendar.set(Calendar.SECOND, 0)
            selectedTimes.add(calendar)

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeView = TextView(this)
            timeView.text = timeFormat.format(calendar.time)
            timeView.textSize = 16f
            timeContainer.addView(timeView)
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
    }

    private fun updateEndDate() {
        val durationText = editTextDuration.text.toString()
        if (durationText.isNotEmpty()) {
            val duration = durationText.toIntOrNull() ?: return
            val endDate = Calendar.getInstance()
            endDate.time = selectedStartDate.time
            endDate.add(Calendar.DAY_OF_YEAR, duration)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            textViewEndDate.text = "End Date: ${formatter.format(endDate.time)}"
        }
    }

    private fun saveReminder() {
        val medicineName = editTextMedicine.text.toString()
        val dosage = editTextDosage.text.toString()
        val durationDays = editTextDuration.text.toString().toIntOrNull() ?: return

        val reminder = Reminder(
            id = existingReminderId ?: 0,
            medicationName = medicineName,
            dosageAmount = dosage,
            startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedStartDate.time),
            duration = durationDays.toString(),
        )

        val db = ReminderDatabase.getDatabase(this)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val reminderDao = db.reminderDao()
                val reminderTimeDao = db.reminderTimeDao()
                val medicationHistoryDao = db.medicationHistoryDao()

                val newReminderId = if (existingReminderId != null) {
                    reminderDao.updateReminder(reminder)
                    existingReminderId!!
                } else {
                    reminderDao.insertReminder(reminder)
                    db.reminderDao().getAllReminders().last().id
                }

                reminderTimeDao.deleteReminderTimesByReminderId(newReminderId)
                val times = selectedTimes.map { ReminderTime(reminderId = newReminderId, timeInMillis = it.timeInMillis) }
                reminderTimeDao.insertReminderTimes(times)

                val historyList = times.map { rt ->
                    com.example.mediassist.data.model.MedicationHistory(
                        medicationName = reminder.medicationName,
                        dosageAmount = reminder.dosageAmount,
                        startDate = reminder.startDate,
                        duration = reminder.duration,
                        timeInMillis = rt.timeInMillis
                    )
                }
                medicationHistoryDao.insertAll(historyList)

                times.forEach { rt ->
                    val timeCalendar = Calendar.getInstance().apply { timeInMillis = rt.timeInMillis }
                    scheduleReminder(medicineName, dosage, timeCalendar, durationDays)
                }
            }

            Toast.makeText(this@ReminderActivity, "Reminder saved successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun scheduleReminder(medicationName: String, dosageAmount: String, calendar: Calendar, durationDays: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (i in 0 until durationDays) {
            val alarmTime = calendar.clone() as Calendar
            alarmTime.add(Calendar.DAY_OF_YEAR, i)

            val intent = Intent(this, ReminderReceiver::class.java).apply {
                putExtra("medicationName", medicationName)
                putExtra("dosageAmount", dosageAmount)
                putExtra("reminderTime", SimpleDateFormat("hh:mm a", Locale.getDefault()).format(alarmTime.time))
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                (medicationName + alarmTime.timeInMillis.toString()).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.timeInMillis, pendingIntent)
            Log.d("Reminder", "Scheduled: ${alarmTime.time}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied. Reminders may not work.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
