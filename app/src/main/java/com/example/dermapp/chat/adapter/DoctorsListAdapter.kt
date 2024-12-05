package com.example.dermapp.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.chat.holder.DoctorsListHolder
import com.example.dermapp.database.Doctor

/**
 * DoctorsListAdapter is a RecyclerView adapter for displaying a list of doctors.
 * It provides functionality to handle item clicks and efficiently update the list of doctors.
 *
 * @param context The context where the adapter is used.
 * @param doctorsList The initial list of doctors to display.
 * @param onDoctorClick A callback function invoked when a doctor item is clicked.
 */
class DoctorsListAdapter(
    private val context: Context,
    private var doctorsList: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorsListHolder>() {

    /**
     * Creates a new ViewHolder for displaying a doctor item.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new View.
     * @return A new instance of DoctorsListHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorsListHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_doctors_list_item, parent, false)
        return DoctorsListHolder(view, context, onDoctorClick)
    }

    /**
     * Binds the data from a doctor to the corresponding ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the doctor in the list.
     */
    override fun onBindViewHolder(holder: DoctorsListHolder, position: Int) {
        holder.bind(doctorsList[position])
    }

    /**
     * Returns the total number of doctors in the list.
     *
     * @return The size of the doctors list.
     */
    override fun getItemCount(): Int = doctorsList.size

    /**
     * Updates the list of doctors and efficiently refreshes the RecyclerView
     * by calculating the differences between the old and new lists.
     *
     * @param newDoctors The new list of doctors.
     */
    fun updateDoctorsList(newDoctors: List<Doctor>) {
        val diffResult = DiffUtil.calculateDiff(DoctorsDiffCallback(doctorsList, newDoctors))
        doctorsList = newDoctors
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * A DiffUtil.Callback implementation to calculate the differences between
     * two lists of doctors.
     *
     * @param oldList The previous list of doctors.
     * @param newList The new list of doctors.
     */
    class DoctorsDiffCallback(
        private val oldList: List<Doctor>,
        private val newList: List<Doctor>
    ) : DiffUtil.Callback() {

        /**
         * Returns the size of the old list.
         */
        override fun getOldListSize(): Int = oldList.size

        /**
         * Returns the size of the new list.
         */
        override fun getNewListSize(): Int = newList.size

        /**
         * Checks if two items represent the same doctor by comparing their IDs.
         *
         * @param oldItemPosition The position in the old list.
         * @param newItemPosition The position in the new list.
         * @return True if the items are the same, false otherwise.
         */
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].doctorId == newList[newItemPosition].doctorId
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
