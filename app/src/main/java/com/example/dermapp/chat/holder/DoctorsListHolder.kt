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

/**
 * ViewHolder for displaying a single doctor's item in the RecyclerView.
 *
 * @param itemView The view of the individual item.
 * @param context The context where the ViewHolder is used.
 * @param onDoctorClick A callback function invoked when the doctor item is clicked.
 */
class DoctorsListHolder(
    itemView: View,
    private val context: Context,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val profileImage: ImageView = itemView.findViewById(R.id.doctorsListItemProfileImage)
    private val firstName: TextView = itemView.findViewById(R.id.doctorsListItemFirstName)
    private val lastName: TextView = itemView.findViewById(R.id.doctorsListItemLastName)
    private val statusIndicator: View = itemView.findViewById(R.id.doctorsListItemStatus)

    /**
     * Binds the doctor data to the ViewHolder and sets up UI components.
     *
     * @param doctor The doctor object containing data to display.
     */
    fun bind(doctor: Doctor) {

        // Debugging log to track the binding process
        Log.d("DoctorsListHolder", "Doctor: ${doctor.firstName} ${doctor.lastName}, isOnline: ${doctor.isOnline}")

        // Setting the first name and last name
        firstName.text = doctor.firstName
        lastName.text = doctor.lastName

        // Loading the profile image using Glide with a placeholder and circular crop
        Glide.with(context)
            .load(doctor.profilePhoto)
            .placeholder(R.drawable.black_account_circle)
            .error(R.drawable.black_account_circle)
            .transform(CircleCrop())
            .into(profileImage)

        // Setting the status indicator based on the doctor's online status
        val statusDrawable = if (doctor.isOnline) {
            R.drawable.status_indicator_background_online
        } else {
            R.drawable.status_indicator_background_offline
        }
        statusIndicator.setBackgroundResource(statusDrawable)

        // Setting up a click listener for the doctor item
        itemView.setOnClickListener { onDoctorClick(doctor) }
    }
}
