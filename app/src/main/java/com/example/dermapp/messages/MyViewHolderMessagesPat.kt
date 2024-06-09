package com.example.dermapp.messages

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

class MyViewHolderMessagesPat(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageDoc : ImageView
    val firstNameDoc : TextView
    val lastNameDoc : TextView
    val idDoc : TextView
    val mailDoc : TextView
    val addressDoc : TextView
    val phoneDoc : TextView
    init {
        imageDoc = itemView.findViewById(R.id.imageViewDocMessagesPat2)
        firstNameDoc = itemView.findViewById(R.id.textViewDocFirstNameMessagesPat)
        lastNameDoc = itemView.findViewById(R.id.textViewDocLastNameMessagesPat)
        idDoc = itemView.findViewById(R.id.textViewDocIdMessagesPat)
        mailDoc = itemView.findViewById(R.id.textViewDocMailMessagesPat)
        addressDoc = itemView.findViewById(R.id.textViewDocAddressMessagesPat)
        phoneDoc = itemView.findViewById(R.id.textViewDocPhoneMessagesPat)
    }
}