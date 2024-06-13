package com.example.dermapp.messages

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

class MyViewHolderMessagesPat(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageDoc: ImageView = itemView.findViewById(R.id.imageViewDocMessagesPat2)
    val firstNameDoc: TextView = itemView.findViewById(R.id.textViewDocFirstNameMessagesPat)
    val lastNameDoc: TextView = itemView.findViewById(R.id.textViewDocLastNameMessagesPat)
    val idDoc: TextView = itemView.findViewById(R.id.textViewDocIdMessagesPat)
    val mailDoc: TextView = itemView.findViewById(R.id.textViewDocMailMessagesPat)
    val addressDoc: TextView = itemView.findViewById(R.id.textViewDocAddressMessagesPat)
    val phoneDoc: TextView = itemView.findViewById(R.id.textViewDocPhoneMessagesPat)
    val newMessageButtonPat: Button = itemView.findViewById(R.id.newMessageButtonPat)
}
