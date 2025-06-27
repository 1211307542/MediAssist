package com.example.mediassist.broadcast

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mediassist.AlarmRingingActivity
import com.example.mediassist.R
import java.text.SimpleDateFormat
import java.util.*

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Broadcast received for medication reminder!")

        val medName = intent.getStringExtra("medicationName") ?: "Medication"
        val dosage = intent.getStringExtra("dosageAmount") ?: "1 tablet"
        val reminderTime = intent.getStringExtra("reminderTime") ?: SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val startDateStr = intent.getStringExtra("startDate")
        val durationStr = intent.getStringExtra("duration")
        val duration = durationStr?.toIntOrNull() ?: 0

        val remainingDays = calculateRemainingDays(startDateStr, duration)

        val notificationIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicationName", medName)
            putExtra("dosageAmount", dosage)
            putExtra("reminderTime", reminderTime)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "med_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medication Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for medication reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentText = "Time to take your $medName ($dosage)."

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Medication Reminder")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Check if notification permission is granted before showing notification
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } else {
            Log.w("ReminderReceiver", "Notification permission not granted")
        }
    }

    private fun calculateRemainingDays(startDateStr: String?, duration: Int): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = sdf.parse(startDateStr ?: "") ?: return 0

            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val startCal = Calendar.getInstance().apply {
                time = startDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val endCal = Calendar.getInstance().apply {
                time = startCal.time
                add(Calendar.DAY_OF_YEAR, duration)
            }

            val diffMillis = endCal.timeInMillis - todayCal.timeInMillis
            val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

            diffDays.coerceAtLeast(0)
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Error calculating remaining days", e)
            0
        }
    }
}
