package com.example.dermapp.startPatient

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder for displaying archival appointments in a RecyclerView.
 */
class MyViewHolderStartPatArchivalAppointment(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstNameDoc : TextView
    val appointmentLoc : TextView
    val appointmentDate : TextView
    val seeDetailsButton: Button

    init {
        firstNameDoc = itemView.findViewById(R.id.textViewDoctorStartPatAppointments)
        appointmentLoc = itemView.findViewById(R.id.textViewAddressStartPatAppointments)
        appointmentDate = itemView.findViewById(R.id.textViewDateStartPatAppointments)
        seeDetailsButton = itemView.findViewById(R.id.seeDetailsButtonStartAppointmentPat)
    }
}


