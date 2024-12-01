@file:Suppress("DEPRECATION")

package com.example.dermapp.chat.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.text.Html
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.example.dermapp.MainActivity
import com.example.dermapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

private const val CHANNEL_ID = "messages_channel"

class FirebaseService : FirebaseMessagingService() {

    companion object {
        private const val REPLY_ACTION_ID = "REPLY_ACTION_ID"
        private const val KEY_REPLY_TEXT = "KEY_REPLY_TEXT"

        var sharedPref: SharedPreferences? = null

        var token: String?
            get() {
                return sharedPref?.getString("token", "")
            }
            set(value) {
                sharedPref?.edit()?.putString("token", value)?.apply()
            }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(this, MainActivity::class.java)
        val notificationID = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        // Obsługa odpowiedzi na powiadomienie
        val remoteInput = RemoteInput.Builder(KEY_REPLY_TEXT)
            .setLabel("Odpowiedz")
            .build()

        val replyIntent = Intent(this, NotificationReply::class.java)
        val replyPendingIntent = PendingIntent.getBroadcast(this, 0, replyIntent, PendingIntent.FLAG_MUTABLE)

        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.reply,
            "Odpowiedz",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        // Konstruowanie powiadomienia
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"] ?: "Nowa wiadomość")
            .setContentText(Html.fromHtml("<b>${message.data["title"]}</b>: ${message.data["message"]}"))
            .setSmallIcon(R.drawable.logo_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(replyAction)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this).notify(notificationID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelName = "Powiadomienia o wiadomościach"
        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Kanał powiadomień dla wiadomości"
            enableLights(true)
            lightColor = Color.GREEN
            enableVibration(true)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    // Funkcja pomocnicza do powiadomienia wysyłającego
    fun notifySender(context: Context, title: String, message: String) {
        val notificationID = Random.nextInt()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.logo_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(notificationID, notification)
    }
}
