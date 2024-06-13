package com.example.dermapp.startPatient

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

class MyViewHolderStartPatAppointment(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstNameDoc : TextView
    val lastNameDoc : TextView
    val appointmentDate : TextView
    val deleteButton: Button
    init {
        firstNameDoc = itemView.findViewById(R.id.textViewDoctorStartPatAppointments)
        lastNameDoc = itemView.findViewById(R.id.textViewSurnameStartPatAppointments)
        appointmentDate = itemView.findViewById(R.id.textViewDateStartPatAppointments)
        deleteButton = itemView.findViewById(R.id.buttonDeleteAppointmentPat)
    }
}