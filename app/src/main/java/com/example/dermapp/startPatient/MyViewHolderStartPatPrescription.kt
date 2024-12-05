package com.example.dermapp.startPatient

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R

/**
 * ViewHolder for displaying prescriptions in a RecyclerView.
 *
 * @param itemView The view representing a single prescription item in the RecyclerView.
 */
class MyViewHolderStartPatPrescription(itemView: View) : RecyclerView.ViewHolder(itemView) {

    // TextView displaying the doctor's first name
    val firstNameDoc: TextView

    // TextView displaying the doctor's last name
    val lastNameDoc: TextView

    // TextView displaying the prescription date
    val prescriptionDate: TextView

    // TextView displaying the prescription text or description
    val prescriptionText: TextView

    // Button for deleting the prescription
    val deleteButton: Button

    /**
     * Initializes the ViewHolder by finding and assigning views by their IDs.
     */
    init {
        firstNameDoc = itemView.findViewById(R.id.textViewDoctorStartPatPrescription2)
        lastNameDoc = itemView.findViewById(R.id.textViewSurnameStartPatPrescription2)
        prescriptionDate = itemView.findViewById(R.id.textViewDateStartPatPrescription)
        prescriptionText = itemView.findViewById(R.id.textTextStartPatPrescription)
        deleteButton = itemView.findViewById(R.id.buttonDeletePatPres)
    }
}
