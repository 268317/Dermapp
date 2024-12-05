package com.example.dermapp.startPatient

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder for displaying current appointments in a RecyclerView.
 *
 * @param itemView The view representing a single appointment item in the RecyclerView.
 */
class MyViewHolderStartPatAppointment(itemView: View) : RecyclerView.ViewHolder(itemView) {

    // TextView displaying the doctor's name
    val firstNameDoc: TextView

    // TextView displaying the appointment date
    val appointmentDate: TextView

    // TextView displaying the appointment location
    val appointmentLoc: TextView

    // Button for deleting the appointment
    val deleteButton: Button

    // Button for viewing details of the appointment
    val seeDetailsButton: Button

    /**
     * Initializes the ViewHolder by finding and assigning views by their IDs.
     */
    init {
        firstNameDoc = itemView.findViewById(R.id.textViewDoctorStartPatAppointments)
        appointmentDate = itemView.findViewById(R.id.textViewDateStartPatAppointments)
        appointmentLoc = itemView.findViewById(R.id.textViewAddressStartPatAppointments)
        deleteButton = itemView.findViewById(R.id.buttonDeleteAppointmentPat)
        seeDetailsButton = itemView.findViewById(R.id.seeDetailsButtonStartAppointmentPat)
    }
}

