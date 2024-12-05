package com.example.dermapp.startPatient

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder for displaying medical reports in a RecyclerView.
 *
 * @param itemView The view representing a single medical report item in the RecyclerView.
 */
class MyViewHolderStartPatReport(itemView: View) : RecyclerView.ViewHolder(itemView) {

    // TextView displaying the doctor's first name
    val firstNameDoc: TextView

    // TextView displaying the report date
    val reportDate: TextView

    // Button for viewing full details of the medical report
    val seeDetailsButton: Button

    /**
     * Initializes the ViewHolder by finding and assigning views by their IDs.
     */
    init {
        firstNameDoc = itemView.findViewById(R.id.textViewDoctorStartPatReports)
        reportDate = itemView.findViewById(R.id.textViewDateStartPatReports)
        seeDetailsButton = itemView.findViewById(R.id.buttonSeeFullReportStartPatReports)
    }
}
