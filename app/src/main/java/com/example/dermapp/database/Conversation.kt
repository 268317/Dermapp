import com.google.firebase.firestore.PropertyName

open class Conversation(
    @get:PropertyName("conversationId") @set:PropertyName("conversationId") open var conversationId: String = "",
    @get:PropertyName("senderId") @set:PropertyName("senderId") open var senderId: String = "",
    @get:PropertyName("receiverId") @set:PropertyName("receiverId") open var receiverId: String = "",
    @get:PropertyName("lastMessage") @set:PropertyName("lastMessage") open var lastMessage: String = "",
    @get:PropertyName("lastMessageTimestamp") @set:PropertyName("lastMessageTimestamp") open var lastMessageTimestamp: com.google.firebase.Timestamp? = null
) {
    constructor() : this("", "", "", "", null)
}

