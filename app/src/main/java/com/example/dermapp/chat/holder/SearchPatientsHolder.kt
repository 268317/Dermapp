package com.example.dermapp.chat.holder

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Patient

class SearchPatientsHolder(
    itemView: View,
    private val onPatientClick: (Patient) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val patientItemName: TextView = itemView.findViewById(R.id.patientItemName)

    @SuppressLint("SetTextI18n")
    fun bind(patient: Patient) {
        // Obsługa brakujących danych
        patientItemName.text = "${patient.firstName} ${patient.lastName}"

        // Obsługa kliknięcia
        itemView.setOnClickListener { onPatientClick(patient) }
    }
}
