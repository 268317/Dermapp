package com.example.dermapp.chat.adapter

import android.content.Context
import android.content.Intent
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

        // Bind data to the view holder
        holder.recentChatsItemName.text = chat.name ?: "Unknown"
        Glide.with(context)
            .load(chat.friendsImage)
            .apply(RequestOptions.placeholderOf(R.drawable.black_account_circle)
                .error(R.drawable.black_account_circle))
            .into(holder.recentChatsItemProfileImage)

        // Navigate to the appropriate chat activity
        holder.itemView.setOnClickListener {
            val intent = if (isDoctor) {
                Intent(context, MessagesActivityDoc::class.java)
            } else {
                Intent(context, MessagesActivityPat::class.java)
            }
            intent.putExtra("conversationId", chat.conversationId)
            intent.putExtra("friendId", chat.friendId)
            context.startActivity(intent)
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
