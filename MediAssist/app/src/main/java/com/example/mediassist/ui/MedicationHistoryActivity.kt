package com.example.mediassist.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediassist.R
import com.example.mediassist.ui.adapter.MedicationHistoryAdapter
import com.example.mediassist.data.ReminderDatabase
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MedicationHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicationHistoryAdapter
    private lateinit var database: ReminderDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_history)

        recyclerView = findViewById(R.id.medicationHistoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val backButton = findViewById<android.widget.ImageView>(R.id.backButton)
        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        database = ReminderDatabase.getDatabase(this)

        lifecycleScope.launch {
            val historyList = database.medicationHistoryDao().getAllHistory()
            adapter = MedicationHistoryAdapter(historyList)
            recyclerView.adapter = adapter
        }
    }
}
