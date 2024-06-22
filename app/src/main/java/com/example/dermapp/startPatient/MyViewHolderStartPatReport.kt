package com.example.dermapp.startPatient

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

class MyViewHolderStartPatReport(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstNameDoc: TextView = itemView.findViewById(R.id.textViewDoctorStartPatReports)
    val lastNameDoc: TextView = itemView.findViewById(R.id.textViewSurnameStartPatReports)
    val reportDate: TextView = itemView.findViewById(R.id.textViewDateStartPatReports)
    val buttonSeeFullReport: Button = itemView.findViewById(R.id.buttonSeeFullReportStartPatReports)
}