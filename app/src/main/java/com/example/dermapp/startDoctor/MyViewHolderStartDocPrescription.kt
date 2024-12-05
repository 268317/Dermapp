package com.example.dermapp.startDoctor

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder class for displaying prescription information in a RecyclerView.
 *
 * This class binds UI components for each prescription item in the RecyclerView
 * and provides direct access to the views for updating and handling user interactions.
 *
 * @param itemView The view representing a single item in the RecyclerView.
 */
class MyViewHolderStartDocPrescription(itemView: View) : RecyclerView.ViewHolder(itemView) {
    /**
     * TextView for displaying the patient's first name.
     */
    val firstNamePat: TextView

    /**
     * TextView for displaying the prescription date.
     */
    val prescriptionDate: TextView

    /**
     * Button for viewing prescription details.
     */
    val detailsButton: Button

    /**
     * Initializes the ViewHolder by finding views in the provided itemView.
     */
    init {
        firstNamePat = itemView.findViewById(R.id.textViewPatientStartDocPrescription)
        prescriptionDate = itemView.findViewById(R.id.textViewDateStartDocPrescription)
        detailsButton = itemView.findViewById(R.id.buttonSeeDetailsStartDocPrescription)
    }
}
