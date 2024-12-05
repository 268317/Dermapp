package com.example.dermapp.chat.holder

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Patient

/**
 * ViewHolder for displaying a single patient in the search results.
 *
 * @param itemView The view of the individual item.
 * @param onPatientClick A callback function invoked when the patient item is clicked.
 */
class SearchPatientsHolder(
    itemView: View,
    private val onPatientClick: (Patient) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val patientItemName: TextView = itemView.findViewById(R.id.patientItemName)

    /**
     * Binds the patient data to the ViewHolder and sets up the UI components.
     *
     * @param patient The patient object containing data to display.
     */
    @SuppressLint("SetTextI18n")
    fun bind(patient: Patient) {
        // Display the patient's first and last name, handling missing data gracefully
        patientItemName.text = "${patient.firstName} ${patient.lastName}".trim()

        // Set a click listener for the patient item
        itemView.setOnClickListener { onPatientClick(patient) }
    }
}
