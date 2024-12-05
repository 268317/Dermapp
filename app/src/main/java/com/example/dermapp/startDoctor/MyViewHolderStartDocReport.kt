package com.example.dermapp.startDoctor

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder class for displaying medical report information in a RecyclerView.
 *
 * This class binds UI components for each medical report item in the RecyclerView
 * and provides direct access to the views for updating and handling user interactions.
 *
 * @param itemView The view representing a single item in the RecyclerView.
 */
class MyViewHolderStartDocReport(itemView: View) : RecyclerView.ViewHolder(itemView) {
    /**
     * TextView for displaying the patient's first name.
     */
    val firstNamePat: TextView

    /**
     * TextView for displaying the date of the medical report.
     */
    val reportDate: TextView

    /**
     * Button for viewing detailed information about the medical report.
     */
    val seeDetailsButton: Button

    /**
     * Initializes the ViewHolder by finding views in the provided itemView.
     */
    init {
        firstNamePat = itemView.findViewById(R.id.textViewPatientStartDocReports)
        reportDate = itemView.findViewById(R.id.textViewDateStartDocReports)
        seeDetailsButton = itemView.findViewById(R.id.buttonSeeFullReportStartDocReports)
    }
}
