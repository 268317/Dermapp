package com.example.dermapp.messages

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder for displaying patient information in a RecyclerView for doctor messages.
 *
 * @param itemView The view corresponding to each item in the RecyclerView.
 */
class MyViewHolderMessagesDoc(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imagePat : ImageView
    val firstNamePat : TextView
    val lastNamePat : TextView

    init {
        imagePat = itemView.findViewById(R.id.imageViewPatMessagesDoc2)
        firstNamePat = itemView.findViewById(R.id.textViewPatFirstNameMessagesDoc)
        lastNamePat = itemView.findViewById(R.id.textViewPatLastNameMessagesDoc)
    }
}