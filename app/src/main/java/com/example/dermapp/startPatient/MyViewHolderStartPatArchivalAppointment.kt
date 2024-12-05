package com.example.dermapp.startPatient

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder for displaying archival appointments in a RecyclerView.
 *
 * @param itemView The view representing a single archival appointment item in the RecyclerView.
 */
class MyViewHolderStartPatArchivalAppointment(itemView: View) : RecyclerView.ViewHolder(itemView) {

    // TextView displaying the doctor's name
    val firstNameDoc: TextView

    // TextView displaying the appointment location
    val appointmentLoc: TextView

    // TextView displaying the appointment date
    val appointmentDate: TextView

    // Button for viewing details of the archival appointment
    val seeDetailsButton: Button

    /**
     * Initializes the ViewHolder by finding and assigning views by their IDs.
     */
    init {
        firstNameDoc = itemView.findViewById(R.id.textViewDoctorStartPatAppointments)
        appointmentLoc = itemView.findViewById(R.id.textViewAddressStartPatAppointments)
        appointmentDate = itemView.findViewById(R.id.textViewDateStartPatAppointments)
        seeDetailsButton = itemView.findViewById(R.id.seeDetailsButtonStartAppointmentPat)
    }
}



