package com.example.dermapp.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermapp.R
import com.example.dermapp.chat.database.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class MessagesAdapter(
    private val context: Context,
    private val messageList: List<Message>,
    private val profilePhotoUrl: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].isSender(currentUserId)) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return if (viewType == VIEW_TYPE_SENT) {
            val view = inflater.inflate(R.layout.chat_right_message, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.chat_left_message, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message, profilePhotoUrl)
        }
    }


    override fun getItemCount(): Int = messageList.size

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTimestamp: TextView = itemView.findViewById(R.id.messageTime)
        private val messageSeen: TextView = itemView.findViewById(R.id.messageSeen)
        private val photoImage: ImageView = itemView.findViewById(R.id.photoImage)

        fun bind(message: Message) {
            if (!message.photoUrl.isNullOrEmpty()) {
                photoImage.visibility = View.VISIBLE
                messageText.visibility = View.GONE
                Glide.with(itemView.context)
                    .load(message.photoUrl)
                    .placeholder(R.drawable.black_account_circle)
                    .into(photoImage)
            } else {
                photoImage.visibility = View.GONE
                messageText.visibility = View.VISIBLE
                messageText.text = message.messageText
            }

            val date = message.timestamp?.toDate()
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            messageTimestamp.text = date?.let { formatter.format(it) } ?: ""
            messageSeen.text = if (message.isRead) "Seen" else "Sent"
        }

    }


    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTimestamp: TextView = itemView.findViewById(R.id.messageTime)
        private val profileImage: ImageView = itemView.findViewById(R.id.leftMessageProfileImage)
        private val photoImage: ImageView = itemView.findViewById(R.id.photoImage)

        fun bind(message: Message, profilePhotoUrl: String?) {
            messageText.text = message.messageText
            if (!message.photoUrl.isNullOrEmpty()) {
                photoImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.photoUrl)
                    .into(photoImage)
            } else {
                photoImage.visibility = View.GONE
            }

            val date = message.timestamp?.toDate()
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            messageTimestamp.text = date?.let { formatter.format(it) } ?: ""

            Glide.with(itemView.context)
                .load(profilePhotoUrl)
                .placeholder(R.drawable.black_account_circle)
                .circleCrop()
                .into(profileImage)
        }

    }

}
