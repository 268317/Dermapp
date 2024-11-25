package com.example.dermapp.messages.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class NewMessageAdapter(
    private val context: Context,
    private var messagesList: List<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val MESSAGE_SENT = 1
    private val MESSAGE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        return if (messagesList[position].senderId == currentUserId) MESSAGE_SENT else MESSAGE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return if (viewType == MESSAGE_SENT) {
            SentMessageViewHolder(inflater.inflate(R.layout.chat_right_message, parent, false))
        } else {
            ReceivedMessageViewHolder(inflater.inflate(R.layout.chat_left_message, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messagesList[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messagesList.size

    /**
     * ViewHolder for sent messages
     */
    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            itemView.findViewById<TextView>(R.id.show_message).text = message.messageText
            itemView.findViewById<TextView>(R.id.timeView).text = message.timestamp?.toDate()?.let { sdf.format(it) }
        }
    }



    /**
     * ViewHolder for received messages
     */
    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: Message) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            itemView.findViewById<TextView>(R.id.show_message).text = message.messageText
            itemView.findViewById<TextView>(R.id.timeView).text = message.timestamp?.toDate()?.let { sdf.format(it) }
        }
    }

    /**
     * Update the list of messages in the adapter and refresh the RecyclerView
     */
    fun updateMessages(newMessagesList: List<Message>) {
        this.messagesList = newMessagesList
        notifyDataSetChanged()
    }
}
