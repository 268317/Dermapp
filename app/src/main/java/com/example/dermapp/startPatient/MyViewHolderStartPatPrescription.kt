package com.example.dermapp.startPatient

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder for displaying prescriptions in a RecyclerView.
 */
class MyViewHolderStartPatPrescription(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstNameDoc : TextView
    val lastNameDoc : TextView
    val prescriptionDate : TextView
    val prescriptionText : TextView
    val deleteButton: Button
    init {
        firstNameDoc = itemView.findViewById(R.id.textViewDoctorStartPatPrescription2)
        lastNameDoc = itemView.findViewById(R.id.textViewSurnameStartPatPrescription2)
        prescriptionDate = itemView.findViewById(R.id.textViewDateStartPatPrescription)
        prescriptionText = itemView.findViewById(R.id.textTextStartPatPrescription)
        deleteButton = itemView.findViewById(R.id.buttonDeletePatPres)
    }
}