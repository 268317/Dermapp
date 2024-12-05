package com.example.dermapp.chat.notifications

import com.example.dermapp.R
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * NotificationReceiver is a BroadcastReceiver that handles the display of notifications.
 * It is triggered by a broadcast intent and displays a notification to the user.
 */
class NotificationReceiver : BroadcastReceiver() {

    /**
     * Called when the BroadcastReceiver receives a broadcast intent.
     * Checks if the app has the necessary notification permissions and displays a notification.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent received by the receiver.
     */
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Attempting to display a notification")

        // Check for POST_NOTIFICATIONS permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("NotificationReceiver", "Missing POST_NOTIFICATIONS permission")
            return // Exit if permission is not granted
        }

        // Build and display the notification
        val builder = NotificationCompat.Builder(context, "reminderChannel")
            .setSmallIcon(R.drawable.logo_foreground) // Set the notification icon
            .setContentTitle("Reminder: Submit Your Report")
            .setContentText("Please add a report from the examination.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(200, builder.build()) // Send the notification
        Log.d("NotificationReceiver", "Notification displayed successfully")
    }
}
