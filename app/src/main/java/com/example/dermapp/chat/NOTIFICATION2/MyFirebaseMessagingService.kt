package com.example.dermapp.chat.NOTIFICATION2

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
 * This service handles incoming Firebase Cloud Messaging (FCM) messages.
 * It processes received messages and displays notifications to the user.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when an FCM message is received.
     * Logs the received data, extracts the sender's name and message content,
     * and triggers a notification display.
     *
     * @param remoteMessage The message received from FCM, containing data and notification payloads.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Otrzymano wiadomość: ${remoteMessage.data}")

        createNotificationChannel()

        val senderName = remoteMessage.data["senderName"] ?: "Nieznany użytkownik"
        val message = remoteMessage.data["message"] ?: "Nowa wiadomość"

        Log.d("FCM", "Nadawca: $senderName, Wiadomość: $message")

        showNotification(senderName, "$senderName wysłał(a) ci wiadomość")
    }

    /**
     * Called when the FCM token is generated or updated.
     * Logs the new token and saves it to Firestore.
     *
     * @param token The new FCM token.
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
     * Displays a notification to the user.
     * Constructs and sends a notification with the given title and body.
     * Checks for notification permissions before displaying the notification.
     *
     * @param title The title of the notification.
     * @param body The content text of the notification.
     */
    private fun showNotification(title: String, body: String) {
        Log.d("FCM", "Wyświetlanie powiadomienia: Tytuł=$title, Treść=$body")
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
            Log.d("FCM", "Powiadomienie wysłane")
        } else {
            Log.e("FCM", "Brak uprawnień do wyświetlenia powiadomienia")
        }
    }
}
