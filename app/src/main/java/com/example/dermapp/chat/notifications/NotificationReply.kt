package com.example.dermapp.chat.notifications

import android.app.NotificationManager
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.dermapp.R
import com.example.dermapp.chat.SharedPrefs
import com.example.dermapp.chat.Utils
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Random

private const val CHANNEL_ID = "my_channel"

class NotificationReply : BroadcastReceiver() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val remoteInput = RemoteInput.getResultsFromIntent(intent)

        if (remoteInput != null) {
            val repliedText = remoteInput.getString("KEY_REPLY_TEXT") ?: return

            val sharedPrefs = SharedPrefs(context)
            val friendId = sharedPrefs.getValue("friendid") ?: return
            val chatRoomId = sharedPrefs.getValue("chatroomid") ?: return
            val friendName = sharedPrefs.getValue("friendname") ?: "Unknown"
            val friendImage = sharedPrefs.getValue("friendimage") ?: ""

            val currentTime = Utils.getTime()

            // Tworzenie wiadomości
            val messageData = hashMapOf(
                "sender" to Utils.getUidLoggedIn(),
                "time" to currentTime,
                "receiver" to friendId,
                "message" to repliedText
            )

            // Zapisanie wiadomości w Firestore
            firestore.collection("messages")
                .document(chatRoomId)
                .collection("conversation")
                .document(currentTime)
                .set(messageData)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        updateConversation(context, friendId, chatRoomId, repliedText, friendName, friendImage)
                    } else {
                        showErrorNotification(context, notificationManager)
                    }
                }
        }
    }

    private fun updateConversation(
        context: Context,
        friendId: String,
        chatRoomId: String,
        repliedText: String,
        friendName: String,
        friendImage: String
    ) {
        val currentTime = Utils.getTime()

        // Ustawienie danych w konwersacji użytkownika
        val userConversationData = hashMapOf(
            "friendid" to friendId,
            "time" to currentTime,
            "sender" to Utils.getUidLoggedIn(),
            "message" to repliedText,
            "friendsimage" to friendImage,
            "name" to friendName,
            "person" to "you"
        )

        firestore.collection("Conversation${Utils.getUidLoggedIn()}")
            .document(friendId)
            .set(userConversationData)

        // Ustawienie danych w konwersacji odbiorcy
        val friendConversationData = hashMapOf(
            "messages" to repliedText,
            "time" to currentTime,
            "person" to friendName
        )

        firestore.collection("Conversation$friendId")
            .document(Utils.getUidLoggedIn())
            .update(friendConversationData as Map<String, Any>)

        // Wyświetlenie powiadomienia o wysłaniu odpowiedzi
        showReplyNotification(context)
    }

    private fun showReplyNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_foreground)
            .setContentText("Reply Sent")
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random().nextInt(), notification)
    }

    private fun showErrorNotification(context: Context, notificationManager: NotificationManager) {
        val errorNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_foreground)
            .setContentText("Error sending reply")
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random().nextInt(), errorNotification)
    }
}
