package com.example.dermapp.chat.holder

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Doctor

/**
 * ViewHolder for displaying a single doctor in the search results.
 *
 * @param itemView The view of the individual item.
 * @param onDoctorClick A callback function invoked when the doctor item is clicked.
 */
class SearchDoctorsHolder(
    itemView: View,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val doctorItemName: TextView = itemView.findViewById(R.id.doctorItemName)

    /**
     * Binds the doctor data to the ViewHolder and sets up the UI components.
     *
     * @param doctor The doctor object containing data to display.
     */
    @SuppressLint("SetTextI18n")
    fun bind(doctor: Doctor) {
        // Display the doctor's first and last name, handling null values gracefully
        doctorItemName.text = "${doctor.firstName} ${doctor.lastName}".trim()

        // Set a click listener for the doctor item
        itemView.setOnClickListener { onDoctorClick(doctor) }
    }
}
