package com.example.dermapp.startDoctor

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

class MyViewHolderStartDocPrescription(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstNamePat : TextView
    //val lastNamePat : TextView
    val prescriptionDate : TextView
    //val prescriptionText : TextView
    val detailsButton: Button
    init {
        firstNamePat = itemView.findViewById(R.id.textViewPatientStartDocPrescription)
        //lastNamePat = itemView.findViewById(R.id.textViewSurnameStartDocPrescription)
        prescriptionDate = itemView.findViewById(R.id.textViewDateStartDocPrescription)
        //prescriptionText = itemView.findViewById(R.id.textTextStartDocPrescription)
        detailsButton = itemView.findViewById(R.id.buttonSeeDetailsStartDocPrescription)
    }
}