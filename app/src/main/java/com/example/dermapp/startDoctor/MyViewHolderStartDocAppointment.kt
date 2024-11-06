package com.example.dermapp.startDoctor

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder class for displaying appointments in a RecyclerView in StartDocActivity.
 * @param itemView The view representing each item in the RecyclerView.
 */
class MyViewHolderStartDocAppointment(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstNamePat : TextView
    //val lastNamePat : TextView
    val appointmentDate : TextView
    val appointmentLoc : TextView
    val deleteButton: Button
    val seeDetailsButton: Button
    init {
        firstNamePat = itemView.findViewById(R.id.textViewPatientStartDocAppointments)
        //lastNamePat = itemView.findViewById(R.id.textViewSurnameStartDocAppointments)
        appointmentDate = itemView.findViewById(R.id.textViewDateStartDocAppointments)
        appointmentLoc = itemView.findViewById(R.id.textViewAddressStartDocAppointments)
        deleteButton = itemView.findViewById(R.id.buttonDeleteAppointmentDoc)
        seeDetailsButton = itemView.findViewById(R.id.seeDetailsButtonStartAppointmentDoc)
    }
}