package com.example.dermapp.messages.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermapp.R
import com.example.dermapp.database.Conversation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RecentChatsAdapter(
    private val context: Context,
    private var conversations: MutableList<Conversation>,
    private val onConversationClick: (Conversation) -> Unit
) : RecyclerView.Adapter<RecentChatsAdapter.RecentChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_recent_chat_item_pat, parent, false)
        return RecentChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentChatViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.bind(conversation, onConversationClick)
    }

    override fun getItemCount(): Int = conversations.size

    fun updateConversations(newConversations: MutableList<Conversation>) {
        conversations = newConversations
        notifyDataSetChanged()
    }

    inner class RecentChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: ImageView = itemView.findViewById(R.id.recentChatImageViewDoc)
        private val userNameTextView: TextView = itemView.findViewById(R.id.recentChatTextNameDoc)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.recentChatTextLastMessageDoc)
        private val timeTextView: TextView = itemView.findViewById(R.id.recentChatTextTimeDoc)

        fun bind(conversation: Conversation, onConversationClick: (Conversation) -> Unit) {
            lastMessageTextView.text = conversation.lastMessage

            // Formatowanie daty ostatniej wiadomości
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.ENGLISH)
            timeTextView.text = conversation.lastMessageTimestamp?.toDate()?.let { dateFormat.format(it) } ?: "--:--"

            // Pobierz dane użytkownika na podstawie doctorId lub patientId
            val otherUserId = if (conversation.patientId == FirebaseAuth.getInstance().currentUser?.uid) {
                conversation.doctorId
            } else {
                conversation.patientId
            }

            FirebaseFirestore.getInstance().collection("users")
                .document(otherUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = "${document.getString("firstName")} ${document.getString("lastName")}"
                        val profilePhoto = document.getString("profilePhoto") ?: ""

                        userNameTextView.text = userName
                        Glide.with(context)
                            .load(profilePhoto)
                            .placeholder(R.drawable.account_circle)
                            .into(profileImageView)
                    }
                }

            itemView.setOnClickListener {
                onConversationClick(conversation)
            }
        }
    }
}
