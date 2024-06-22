package com.example.dermapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("onReceive", "I want to send notification: ${intent}")

        val notificationBuilder = NotificationCompat.Builder(context, "ChannelId")
        notificationBuilder
            .setSmallIcon(R.drawable.ic_appointment)
            .setContentTitle("Appointment reminder")
            .setContentText("Less than 24 hours left until the scheduled visit. If you cannot come, please cancel your visit.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Please login to check your appointment details."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)

        // Sprawdzenie uprawnień do wysyłania powiadomień
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("onReceive", "I didn't send notification - I don't have permission.")
            return
        }
        Log.d("onReceive", "I've sent notification.")
        // Wyślij powiadomienie
        manager.notify(200, notificationBuilder.build())
    }

}