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
import com.example.dermapp.database.Doctor

class DoctorsListHolder(
    itemView: View,
    private val context: Context,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val profileImage: ImageView = itemView.findViewById(R.id.doctorsListItemProfileImage)
    private val firstName: TextView = itemView.findViewById(R.id.doctorsListItemFirstName)
    private val lastName: TextView = itemView.findViewById(R.id.doctorsListItemLastName)
    private val statusIndicator: View = itemView.findViewById(R.id.doctorsListItemStatus)

    fun bind(doctor: Doctor) {

        // Debugging log
        Log.d("DoctorsListHolder", "Doctor: ${doctor.firstName} ${doctor.lastName}, isOnline: ${doctor.isOnline}")


        // Ustawianie imienia i nazwiska
        firstName.text = doctor.firstName
        lastName.text = doctor.lastName

        // Ładowanie zdjęcia profilowego za pomocą Glide
        Glide.with(context)
            .load(doctor.profilePhoto)
            .placeholder(R.drawable.black_account_circle)
            .error(R.drawable.black_account_circle)
            .transform(CircleCrop())
            .into(profileImage)

        // Ustawianie wskaźnika statusu
        val statusDrawable = if (doctor.isOnline) {
            R.drawable.status_indicator_background_online
        } else {
            R.drawable.status_indicator_background_offline
        }
        statusIndicator.setBackgroundResource(statusDrawable)

        // Obsługa kliknięcia
        itemView.setOnClickListener { onDoctorClick(doctor) }
    }
}
