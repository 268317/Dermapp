package com.example.dermapp.messages

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

class MyViewHolderMessagesDoc(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imagePat : ImageView
    val firstNamePat : TextView
    val lastNamePat : TextView
    val peselPat : TextView
    val mailPat : TextView
    val addressPat : TextView
    val phonePat : TextView
    init {
        imagePat = itemView.findViewById(R.id.imageViewPatMessagesDoc2)
//        imagePatMail = itemView.findViewById(R.id.imageViewPatMessagesDoc3)
//        imagePatLocalization = itemView.findViewById(R.id.imageViewPatMessagesDoc4)
//        imagePatPhone = itemView.findViewById(R.id.imageViewPatMessagesDoc5)
        firstNamePat = itemView.findViewById(R.id.textViewPatFirstNameMessagesDoc)
        lastNamePat = itemView.findViewById(R.id.textViewPatLastNameMessagesDoc)
        peselPat = itemView.findViewById(R.id.textViewPeselMessagesDoc)
        mailPat = itemView.findViewById(R.id.textViewPatMailMessagesDoc)
        addressPat = itemView.findViewById(R.id.textViewPatAddressMessagesDoc)
        phonePat = itemView.findViewById(R.id.textViewPatPhoneMessagesDoc)
    }
}