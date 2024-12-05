package com.example.dermapp.startDoctor

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder class for displaying archival appointment information in a RecyclerView.
 *
 * This class binds UI components for each archival appointment item in the RecyclerView
 * and provides direct access to the views for updating and handling user interactions.
 *
 * @param itemView The view representing a single item in the RecyclerView.
 */
class MyViewHolderStartDocArchivalAppointment(itemView: View) : RecyclerView.ViewHolder(itemView) {
    /**
     * TextView for displaying the patient's first name.
     */
    val firstNamePat: TextView

    /**
     * TextView for displaying the appointment date.
     */
    val appointmentDate: TextView

    /**
     * Button for editing the archival appointment details.
     */
    val editButton: Button

    /**
     * Button for viewing the archival appointment details.
     */
    val seeDetailsButton: Button

    /**
     * Initializes the ViewHolder by finding views in the provided itemView.
     */
    init {
        firstNamePat = itemView.findViewById(R.id.textViewPatientStartDocAppointments)
        appointmentDate = itemView.findViewById(R.id.textViewDateStartDocAppointments)
        editButton = itemView.findViewById(R.id.buttonDeleteAppointmentDoc)
        seeDetailsButton = itemView.findViewById(R.id.seeDetailsButtonStartAppointmentDoc)
    }
}
