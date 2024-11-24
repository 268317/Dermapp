package com.example.dermapp.messages.holder

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
    val imagePat: ImageView = itemView.findViewById(R.id.imageViewPatMessagesDoc2)
    val firstNamePat: TextView = itemView.findViewById(R.id.textViewPatFirstNameMessagesDoc)
    val lastNamePat: TextView = itemView.findViewById(R.id.textViewPatLastNameMessagesDoc)
    val statusIndicatorDoc: View = itemView.findViewById(R.id.statusIndicatorDoc)
}