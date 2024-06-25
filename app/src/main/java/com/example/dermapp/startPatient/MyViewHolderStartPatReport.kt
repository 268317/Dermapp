package com.example.dermapp.startPatient

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder for displaying medical reports in a RecyclerView.
 */
class MyViewHolderStartPatReport(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstNameDoc : TextView
    val lastNameDoc : TextView
    val reportDate : TextView
    val seeDetailsButton: Button
    init {
        firstNameDoc = itemView.findViewById(R.id.textViewDoctorStartPatReports)
        lastNameDoc = itemView.findViewById(R.id.textViewSurnameStartPatReports)
        reportDate = itemView.findViewById(R.id.textViewDateStartPatReports)
        seeDetailsButton = itemView.findViewById(R.id.buttonSeeFullReportStartPatReports)
    }
}