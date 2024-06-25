package com.example.dermapp.messages

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder for displaying doctor information in a RecyclerView for patient messages.
 *
 * @param itemView The view corresponding to each item in the RecyclerView.
 */
class MyViewHolderMessagesPat(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageDoc: ImageView = itemView.findViewById(R.id.imageViewDocMessagesPat2)
    val firstNameDoc: TextView = itemView.findViewById(R.id.textViewDocFirstNameMessagesPat)
    val lastNameDoc: TextView = itemView.findViewById(R.id.textViewDocLastNameMessagesPat)
    val mailDoc: TextView = itemView.findViewById(R.id.textViewDocMailMessagesPat)
    val phoneDoc: TextView = itemView.findViewById(R.id.textViewDocPhoneMessagesPat)
    val newMessageButtonPat: Button = itemView.findViewById(R.id.newMessageButtonPat)
}
