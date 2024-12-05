package com.example.dermapp.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.chat.holder.PatientsListHolder
import com.example.dermapp.database.Patient

/**
 * PatientsListAdapter is a RecyclerView adapter for displaying a list of patients.
 * It supports item click handling and efficient list updates with DiffUtil.
 *
 * @param context The context where the adapter is used.
 * @param patientsList The initial list of patients to display.
 * @param onPatientClick A callback function invoked when a patient item is clicked.
 */
class PatientsListAdapter(
    private val context: Context,
    private var patientsList: List<Patient>,
    private val onPatientClick: (Patient) -> Unit
) : RecyclerView.Adapter<PatientsListHolder>() {

    /**
     * Creates a new ViewHolder for displaying a patient item.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new instance of PatientsListHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientsListHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_patients_list_item, parent, false)
        return PatientsListHolder(view, context, onPatientClick)
    }

    /**
     * Binds the data from a patient to the corresponding ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the patient in the list.
     */
    override fun onBindViewHolder(holder: PatientsListHolder, position: Int) {
        holder.bind(patientsList[position])
    }

    /**
     * Returns the total number of patients in the list.
     *
     * @return The size of the patients list.
     */
    override fun getItemCount(): Int = patientsList.size

    /**
     * Updates the list of patients and refreshes the RecyclerView efficiently using DiffUtil.
     *
     * @param newPatients The new list of patients.
     */
    fun updatePatientsList(newPatients: List<Patient>) {
        val diffResult = DiffUtil.calculateDiff(PatientsDiffCallback(patientsList, newPatients))
        patientsList = newPatients
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * A DiffUtil.Callback implementation to calculate the differences between
     * two lists of patients.
     *
     * @param oldList The previous list of patients.
     * @param newList The new list of patients.
     */
    class PatientsDiffCallback(
        private val oldList: List<Patient>,
        private val newList: List<Patient>
    ) : DiffUtil.Callback() {

        /**
         * Returns the size of the old list.
         *
         * @return The size of the old list.
         */
        override fun getOldListSize(): Int = oldList.size

        /**
         * Returns the size of the new list.
         *
         * @return The size of the new list.
         */
        override fun getNewListSize(): Int = newList.size

        /**
         * Checks if two items represent the same patient by comparing their unique IDs.
         *
         * @param oldItemPosition The position in the old list.
         * @param newItemPosition The position in the new list.
         * @return True if the items are the same, false otherwise.
         */
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].appUserId == newList[newItemPosition].appUserId
        }

        /**
         * Checks if two items have the same content.
         *
         * @param oldItemPosition The position in the old list.
         * @param newItemPosition The position in the new list.
         * @return True if the contents are the same, false otherwise.
         */
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
