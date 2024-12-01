package com.example.dermapp.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.chat.holder.PatientsListHolder
import com.example.dermapp.database.Patient

class PatientsListAdapter(
    private val context: Context,
    private var patientsList: List<Patient>,
    private val onPatientClick: (Patient) -> Unit
) : RecyclerView.Adapter<PatientsListHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientsListHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_patients_list_item, parent, false)
        return PatientsListHolder(view, context, onPatientClick)
    }

    override fun onBindViewHolder(holder: PatientsListHolder, position: Int) {
        holder.bind(patientsList[position])
    }

    override fun getItemCount(): Int = patientsList.size

    /**
     * Updates the list of patients and refreshes the RecyclerView efficiently using DiffUtil.
     */
    fun updatePatientsList(newPatients: List<Patient>) {
        val diffResult = DiffUtil.calculateDiff(PatientsDiffCallback(patientsList, newPatients))
        patientsList = newPatients
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * DiffUtil Callback for calculating the difference between old and new patient lists.
     */
    class PatientsDiffCallback(
        private val oldList: List<Patient>,
        private val newList: List<Patient>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].appUserId == newList[newItemPosition].appUserId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
