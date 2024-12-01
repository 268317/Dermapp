package com.example.dermapp.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.chat.holder.SearchPatientsHolder
import com.example.dermapp.database.Patient

class SearchPatientsAdapter(
    private val context: Context,
    private var patientsList: List<Patient>,
    private val onPatientClick: (Patient) -> Unit
) : RecyclerView.Adapter<SearchPatientsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPatientsHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_item_patient, parent, false)
        return SearchPatientsHolder(view, onPatientClick)
    }

    override fun onBindViewHolder(holder: SearchPatientsHolder, position: Int) {
        holder.bind(patientsList[position])
    }

    override fun getItemCount(): Int = patientsList.size

    /**
     * Updates the list of patients dynamically using DiffUtil.
     */
    fun setPatientsList(newList: List<Patient>) {
        val diffResult = DiffUtil.calculateDiff(PatientsDiffCallback(patientsList, newList))
        patientsList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * DiffUtil Callback for efficiently updating the RecyclerView.
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
