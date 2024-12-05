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

/**
 * RecentChatsAdapter is a RecyclerView adapter for displaying a list of recent conversations.
 * It handles user-specific data such as last messages, timestamps, and profile images.
 *
 * @param context The context where the adapter is used.
 * @param chatList The initial list of conversations to display.
 * @param isDoctor Indicates whether the current user is a doctor (true) or a patient (false).
 */
class RecentChatsAdapter(
    private val context: Context,
    private var chatList: List<Conversation>,
    private val isDoctor: Boolean
) : RecyclerView.Adapter<RecentChatsAdapter.RecentChatViewHolder>() {

    /**
     * Creates a new ViewHolder for displaying a conversation item.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new instance of RecentChatViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_recent_chats_item, parent, false)
        return RecentChatViewHolder(view)
    }

    /**
     * Binds the data from a conversation to the corresponding ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the conversation in the list.
     */
    override fun onBindViewHolder(holder: RecentChatViewHolder, position: Int) {
        val chat = chatList[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Fetching the last message for the conversation
        chat.getLastMessageId { lastMessageId ->
            if (lastMessageId != null) {
                FirebaseFirestore.getInstance().collection("messages")
                    .document(lastMessageId)
                    .get()
                    .addOnSuccessListener { document ->
                        val isRead = document.getBoolean("isRead") ?: true
                        val senderId = document.getString("senderId") ?: ""

                        // Highlight unread messages for the current user
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

        // Assigning friend name, profile image, and last message details
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

            // Fetch friend details and open the appropriate messaging activity
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

    /**
     * Returns the total number of conversations in the list.
     *
     * @return The size of the conversation list.
     */
    override fun getItemCount(): Int = chatList.size

    /**
     * Updates the list of conversations dynamically using DiffUtil.
     *
     * @param newChatList The new list of conversations.
     */
    fun updateChatList(newChatList: List<Conversation>) {
        val diffResult = DiffUtil.calculateDiff(ChatsDiffCallback(chatList, newChatList))
        chatList = newChatList
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * ViewHolder for displaying a recent chat item.
     */
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

        /**
         * Returns the size of the old list.
         *
         * @return The size of the old list.
         */
        override fun getOldListSize(): Int = oldList.size

        /**
         * Returns the size of the new list.
         *
         * @return The size of the new list.
         */
        override fun getNewListSize(): Int = newList.size

        /**
         * Checks if two items represent the same conversation by comparing their IDs.
         *
         * @param oldItemPosition The position in the old list.
         * @param newItemPosition The position in the new list.
         * @return True if the items are the same, false otherwise.
         */
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].conversationId == newList[newItemPosition].conversationId
        }

        /**
         * Checks if two items have the same content.
         *
         * @param oldItemPosition The position in the old list.
         * @param newItemPosition The position in the new list.
         * @return True if the contents are the same, false otherwise.
         */
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
