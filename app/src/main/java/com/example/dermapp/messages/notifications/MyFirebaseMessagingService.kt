package com.example.dermapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FirebaseMessaging"
        private const val CHANNEL_ID = "default_channel"
        private const val CHANNEL_NAME = "Default Channel"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New token: $token")
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Obsługa powiadomienia
        remoteMessage.notification?.let {
            val title = it.title ?: "Nowa wiadomość"
            val message = it.body ?: "Masz nową wiadomość."
            showNotification(title, message)
        }

        // Obsługa danych w wiadomości
        remoteMessage.data.takeIf { it.isNotEmpty() }?.let { data ->
            Log.d(TAG, "Data payload: $data")
            handleDataMessage(data)
        }
    }

    /**
     * Wyświetlanie powiadomienia
     */
    private fun showNotification(title: String, message: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tworzenie kanału powiadomień (dla Androida 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Akcja po kliknięciu powiadomienia
        val intent = Intent(this, MainActivity::class.java) // Zmień na odpowiednią aktywność
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Budowanie powiadomienia
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.logo_foreground) // Zamień na odpowiednią ikonę
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /**
     * Zapisywanie tokena FCM w Firestore
     */
    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "Token zapisany w Firestore.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Nie udało się zapisać tokena: ${e.message}")
                }
        } else {
            Log.w(TAG, "Użytkownik niezalogowany, token nie został zapisany.")
        }
    }

    /**
     * Obsługa wiadomości zawierającej dane
     */
    private fun handleDataMessage(data: Map<String, String>) {
        // Przykład obsługi danych
        val extraInfo = data["extraInfo"]
        Log.d(TAG, "Obsługa danych wiadomości: extraInfo = $extraInfo")
        // Dodaj tutaj logikę w zależności od potrzeb aplikacji
    }
}
