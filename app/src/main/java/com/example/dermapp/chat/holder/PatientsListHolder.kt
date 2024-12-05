package com.example.dermapp.chat.holder

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.dermapp.R
import com.example.dermapp.database.Patient

class PatientsListHolder(
    itemView: View,
    private val context: Context,
    private val onPatientClick: (Patient) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val profileImage: ImageView = itemView.findViewById(R.id.patientsListItemProfileImage)
    private val firstName: TextView = itemView.findViewById(R.id.patientsListItemFirstName)
    private val lastName: TextView = itemView.findViewById(R.id.patientsListItemLastName)
    private val statusIndicator: View = itemView.findViewById(R.id.patientsListItemStatus)

    fun bind(patient: Patient) {

        // Debugging log
        Log.d("PatientsListHolder", "Doctor: ${patient.firstName} ${patient.lastName}, isOnline: ${patient.isOnline}")


        // Ustawianie imienia i nazwiska
        firstName.text = patient.firstName
        lastName.text = patient.lastName

        // Ładowanie zdjęcia profilowego za pomocą Glide
        Glide.with(context)
            .load(patient.profilePhoto)
            .placeholder(R.drawable.black_account_circle)
            .error(R.drawable.black_account_circle)
            .transform(CircleCrop())
            .into(profileImage)

        // Ustawianie wskaźnika statusu
        val statusDrawable = if (patient.isOnline) {
            R.drawable.status_indicator_background_online
        } else {
            R.drawable.status_indicator_background_offline
        }
        statusIndicator.setBackgroundResource(statusDrawable)

        // Obsługa kliknięcia
        itemView.setOnClickListener { onPatientClick(patient) }
    }
}
