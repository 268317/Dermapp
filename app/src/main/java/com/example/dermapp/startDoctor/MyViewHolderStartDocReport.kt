package com.example.dermapp.startDoctor

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder class for displaying medical reports in a RecyclerView in StartDocActivity.
 * @param itemView The view representing each item in the RecyclerView.
 */
class MyViewHolderStartDocReport(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val firstNamePat: TextView
    // val lastNamePat : TextView
    val reportDate: TextView
    val seeDetailsButton: Button

    init {
        firstNamePat = itemView.findViewById(R.id.textViewPatientStartDocReports)
        // lastNamePat = itemView.findViewById(R.id.textViewSurnameStartDocReports)
        reportDate = itemView.findViewById(R.id.textViewDateStartDocReports)
        seeDetailsButton = itemView.findViewById(R.id.buttonSeeFullReportStartDocReports)
    }
}
