package com.example.dermapp.startPatient

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

class MyViewHolderStartPatPrescription(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstNameDoc : TextView
    val lastNameDoc : TextView
    val prescriptionDate : TextView
    val prescriptionText : TextView
    init {
        firstNameDoc = itemView.findViewById(R.id.textViewDoctorStartPatPrescription)
        lastNameDoc = itemView.findViewById(R.id.textViewSurnameStartPatPrescription)
        prescriptionDate = itemView.findViewById(R.id.textViewDateStartPatPrescription)
        prescriptionText = itemView.findViewById(R.id.textTextStartPatPrescription)
    }
}