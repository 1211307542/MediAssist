package com.example.mediassist.ui

import com.example.mediassist.ReminderAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.R
import com.example.mediassist.ReminderActivity
import com.example.mediassist.data.model.Reminder
import com.example.mediassist.data.model.ReminderTime
import com.example.mediassist.data.ReminderDao
import com.example.mediassist.data.ReminderDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.ImageView

class ReminderListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reminderDao: ReminderDao
    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<Reminder>()
    private var reminderTimesMap: Map<Int, List<ReminderTime>> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_list)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewReminders)
        recyclerView.layoutManager = LinearLayoutManager(this)
        reminderDao = ReminderDatabase.getDatabase(this).reminderDao()

        loadReminders()

        val fabAddReminder: FloatingActionButton = findViewById(R.id.fabAddReminder)
        fabAddReminder.setOnClickListener {
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadReminders() {
        lifecycleScope.launch {
            val db = ReminderDatabase.getDatabase(this@ReminderListActivity)
            val (reminderList, timesMap) = withContext(Dispatchers.IO) {
                val allReminders = db.reminderDao().getAllReminders()
                val timesMap = allReminders.associateWith { reminder ->
                    db.reminderTimeDao().getReminderTimes(reminder.id)
                }.mapKeys { it.key.id }
                Pair(allReminders, timesMap)
            }

            reminders.clear()
            reminders.addAll(reminderList)
            reminderTimesMap = timesMap

            adapter = ReminderAdapter(
                this@ReminderListActivity,
                reminders,
                reminderTimesMap,
                onUpdate = { reminder ->
                    val intent = Intent(this@ReminderListActivity, ReminderActivity::class.java)
                    intent.putExtra("reminder", reminder)
                    startActivity(intent)
                },
                onDelete = { reminder ->
                    AlertDialog.Builder(this@ReminderListActivity)
                        .setTitle("Delete Reminder")
                        .setMessage("Are you sure you want to delete ${reminder.medicationName}?")
                        .setPositiveButton("Yes") { _, _ ->
                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    val db = ReminderDatabase.getDatabase(this@ReminderListActivity)
                                    db.reminderDao().deleteReminder(reminder)
                                    db.reminderTimeDao().deleteReminderTimesByReminderId(reminder.id)
                                }
                                loadReminders()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            )

            recyclerView.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        reminders.clear()
        loadReminders()
    }
}
