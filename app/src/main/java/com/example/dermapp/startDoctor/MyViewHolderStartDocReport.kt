package com.example.dermapp.startDoctor

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

class MyViewHolderStartDocReport(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstNamePat : TextView
    val lastNamePat : TextView
    val reportDate : TextView
    init {
        firstNamePat = itemView.findViewById(R.id.textViewPatientStartDocReports)
        lastNamePat = itemView.findViewById(R.id.textViewSurnameStartDocReports)
        reportDate = itemView.findViewById(R.id.textViewDateStartDocReports)
    }
}