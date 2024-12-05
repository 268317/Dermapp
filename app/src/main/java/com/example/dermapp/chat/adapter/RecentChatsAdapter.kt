package com.example.dermapp.chat.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.dermapp.R
import com.example.dermapp.chat.activity.MessagesActivityDoc
import com.example.dermapp.chat.activity.MessagesActivityPat
import com.example.dermapp.chat.database.Conversation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecentChatsAdapter(
    private val context: Context,
    private var chatList: List<Conversation>,
    private val isDoctor: Boolean // Determines if the user is a doctor or patient
) : RecyclerView.Adapter<RecentChatsAdapter.RecentChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_recent_chats_item, parent, false)
        return RecentChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentChatViewHolder, position: Int) {
        val chat = chatList[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Pobieranie ostatniej wiadomości
        chat.getLastMessageId { lastMessageId ->
            if (lastMessageId != null) {
                FirebaseFirestore.getInstance().collection("messages")
                    .document(lastMessageId)
                    .get()
                    .addOnSuccessListener { document ->
                        val isRead = document.getBoolean("isRead") ?: true
                        val senderId = document.getString("senderId") ?: ""

                        // Sprawdzenie, czy wiadomość jest DO nas (nie jesteśmy nadawcą)
                        val isUnreadForUs = !isRead && senderId != currentUserId

                        holder.itemView.setBackgroundResource(
                            if (isUnreadForUs) R.color.unread_message_background else R.color.read_message_background
                        )
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
        }

        // Reszta logiki przypisywania nazw i zdjęć profilowych
        chat.getFriendUsername(currentUserId) { friendName ->
            holder.recentChatsItemName.text = friendName ?: "Unknown"
        }

        chat.getFriendProfilePhoto(currentUserId) { profilePhoto ->
            Glide.with(context)
                .load(profilePhoto)
                .apply(
                    RequestOptions.placeholderOf(R.drawable.black_account_circle)
                        .error(R.drawable.black_account_circle)
                )
                .circleCrop()
                .into(holder.recentChatsItemProfileImage)
        }

        chat.getLastMessageText { lastMessage ->
            holder.recentChatsItemLastMessageText.text = lastMessage ?: ""
        }

        chat.getLastMessageTimestamp { formattedDate ->
            holder.recentChatsItemLastMessageTime.text = formattedDate ?: ""
        }

        holder.itemView.setOnClickListener {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val friendId = chat.getFriendId(currentUserId) ?: return@setOnClickListener

            // Pobierz dane użytkownika (lekarza/pacjenta)
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(friendId)
                .get()
                .addOnSuccessListener { document ->
                    val friendName = "${document.getString("firstName")} ${document.getString("lastName")}"
                    val friendProfilePhoto = document.getString("profilePhoto")

                    val intent = if (isDoctor) {
                        Intent(context, MessagesActivityDoc::class.java)
                    } else {
                        Intent(context, MessagesActivityPat::class.java)
                    }

                    intent.putExtra("conversationId", chat.conversationId)
                    intent.putExtra("friendId", friendId)
                    intent.putExtra("friendName", friendName)
                    intent.putExtra("friendProfilePhoto", friendProfilePhoto)

                    context.startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Log.e("RecentChatsAdapter", "Error fetching friend data", e)
                }
        }

    }


    override fun getItemCount(): Int = chatList.size

    /**
     * Updates the list of conversations dynamically using DiffUtil.
     */
    fun updateChatList(newChatList: List<Conversation>) {
        val diffResult = DiffUtil.calculateDiff(ChatsDiffCallback(chatList, newChatList))
        chatList = newChatList
        diffResult.dispatchUpdatesTo(this)
    }

    inner class RecentChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recentChatsItemName: TextView = itemView.findViewById(R.id.recentChatsItemName)
        val recentChatsItemProfileImage: ImageView = itemView.findViewById(R.id.recentChatsItemProfileImage)
        val recentChatsItemLastMessageText: TextView = itemView.findViewById(R.id.recentChatsItemLastMessage)
        val recentChatsItemLastMessageTime: TextView = itemView.findViewById(R.id.recentChatsItemTime)
    }

    /**
     * DiffUtil Callback for efficiently updating the RecyclerView.
     */
    class ChatsDiffCallback(
        private val oldList: List<Conversation>,
        private val newList: List<Conversation>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].conversationId == newList[newItemPosition].conversationId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
