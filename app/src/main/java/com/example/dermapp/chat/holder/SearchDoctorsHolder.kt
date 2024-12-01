package com.example.dermapp.chat.holder

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Doctor

class SearchDoctorsHolder(
    itemView: View,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val doctorItemName: TextView = itemView.findViewById(R.id.doctorItemName)

    @SuppressLint("SetTextI18n")
    fun bind(doctor: Doctor) {
        // Wyświetlanie imienia i nazwiska lekarza z obsługą null
        doctorItemName.text = "${doctor.firstName} ${doctor.lastName}"

        // Obsługa kliknięcia na element listy
        itemView.setOnClickListener { onDoctorClick(doctor) }
    }
}
