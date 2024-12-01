package com.example.dermapp.chat.database

import android.os.Parcel
import android.os.Parcelable

data class Conversation(val conversationId : String? ="",
                        val friendId : String? ="",
                        val friendsImage: String? = "",
                        val lastMessageTime : String? = "",
                        val name: String? ="",
                        val senderId: String? = "",
                        val lastMessageText : String? = "",
                        val person: String? = "",
                        val status: String? ="",

                       ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(conversationId)
        parcel.writeString(friendId)
        parcel.writeString(friendsImage)
        parcel.writeString(lastMessageTime)
        parcel.writeString(name)
        parcel.writeString(senderId)
        parcel.writeString(lastMessageText)
        parcel.writeString(person)
        parcel.writeString(status)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel): Conversation {
            return Conversation(parcel)
        }

        override fun newArray(size: Int): Array<Conversation?> {
            return arrayOfNulls(size)
        }
    }


}

//import com.google.firebase.firestore.PropertyName
//
//open class Conversation(
//    @get:PropertyName("conversationId") @set:PropertyName("conversationId") var conversationId: String = "",
//    @get:PropertyName("friendId") @set:PropertyName("friendId") var friendId: String = "",
//    @get:PropertyName("patientId") @set:PropertyName("patientId") var patientId: String = "",
//    @get:PropertyName("lastMessage") @set:PropertyName("lastMessage") var lastMessage: String = "",
//    @get:PropertyName("lastMessageTimestamp") @set:PropertyName("lastMessageTimestamp") var lastMessageTimestamp: com.google.firebase.Timestamp? = null,
//    @get:PropertyName("hasNewMessage") @set:PropertyName("hasNewMessage") var hasNewMessage: Boolean = false,
//) {
//    constructor() : this("", "", "", "", null, false)
//}


