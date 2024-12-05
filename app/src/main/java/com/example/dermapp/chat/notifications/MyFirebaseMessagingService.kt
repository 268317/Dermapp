package com.example.dermapp.chat.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dermapp.R
import com.example.dermapp.chat.activity.MessagesActivityPat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * A Firebase Messaging Service that handles incoming FCM messages and token updates.
 * It processes incoming notifications and manages the FCM device token in Firestore.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Handles the receipt of an FCM message.
     * Logs the received data and displays a notification based on the message content.
     *
     * @param remoteMessage The incoming FCM message containing data and notification payloads.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message received: ${remoteMessage.data}")

        createNotificationChannel()

        val senderName = remoteMessage.data["senderName"] ?: "Unknown User"
        val message = remoteMessage.data["message"] ?: "New message"

        Log.d("FCM", "Sender: $senderName, Message: $message")

        showNotification(senderName, "$senderName sent you a message")
    }

    /**
     * Called when a new FCM token is generated.
     * Logs the new token and updates it in Firestore for the current user.
     *
     * @param token The newly generated FCM token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token generated: $token")

        saveTokenToFirestore(token)
    }

    /**
     * Saves the device token to Firestore under the current user's document.
     *
     * @param token The FCM token to be saved.
     */
    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userDoc = FirebaseFirestore.getInstance().collection("users").document(userId)
            userDoc.update("deviceToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token saved to Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error saving token to Firestore", e)
                }
        } else {
            Log.e("FCM", "User is not authenticated, cannot save token")
        }
    }

    /**
     * Creates a notification channel for displaying message notifications.
     * This is required for devices running Android Oreo (API level 26) or higher.
     */
    private fun createNotificationChannel() {
        val channelId = "messages_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Messages Channel"
            val descriptionText = "Channel for message notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Displays a notification to the user based on the provided title and body.
     * Ensures proper permissions are checked before showing the notification.
     *
     * @param title The title of the notification.
     * @param body The content of the notification.
     */
    private fun showNotification(title: String, body: String) {
        Log.d("FCM", "Displaying notification: Title=$title, Body=$body")
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = "messages_channel"

        val intent = Intent(this, MessagesActivityPat::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d("FCM", "Notification sent successfully")
        } else {
            Log.e("FCM", "Notification permission not granted")
        }
    }
}
