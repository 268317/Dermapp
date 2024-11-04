package com.example.dermapp.startPatient

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder for displaying current appointments in a RecyclerView.
 */
class MyViewHolderStartPatAppointment(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstNameDoc : TextView
    val appointmentDate : TextView
    val deleteButton: Button
    val seeDetailsButton: Button

    init {
        firstNameDoc = itemView.findViewById(R.id.textViewDoctorStartPatAppointments)
        appointmentDate = itemView.findViewById(R.id.textViewDateStartPatAppointments)
        deleteButton = itemView.findViewById(R.id.buttonDeleteAppointmentPat)
        seeDetailsButton = itemView.findViewById(R.id.seeDetailsButtonStartAppointmentPat)
    }
}


