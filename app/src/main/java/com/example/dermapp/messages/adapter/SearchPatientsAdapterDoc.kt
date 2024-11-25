package com.example.dermapp.messages.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Patient

class SearchPatientsAdapterDoc(
    private val context: Context,
    private var patientsList: MutableList<Patient>,
    private val onPatientClick: (Patient) -> Unit
) : RecyclerView.Adapter<SearchPatientsAdapterDoc.PatientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patientsList[position]
        holder.bind(patient)
    }

    override fun getItemCount(): Int = patientsList.size

    fun setPatientsList(newList: MutableList<Patient>) {
        patientsList = newList
        notifyDataSetChanged()
    }

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.patientName)

        fun bind(patient: Patient) {
            nameTextView.text = "${patient.firstName} ${patient.lastName}"
            itemView.setOnClickListener { onPatientClick(patient) }
        }
    }
}
