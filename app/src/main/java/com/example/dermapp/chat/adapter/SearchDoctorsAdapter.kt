package com.example.dermapp.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.chat.holder.SearchDoctorsHolder
import com.example.dermapp.database.Doctor

/**
 * SearchDoctorsAdapter is a RecyclerView adapter for displaying a searchable list of doctors.
 * It supports efficient updates to the list using DiffUtil and handles doctor item clicks.
 *
 * @param context The context where the adapter is used.
 * @param doctorsList The initial list of doctors to display.
 * @param onDoctorClick A callback function invoked when a doctor item is clicked.
 */
class SearchDoctorsAdapter(
    private val context: Context,
    private var doctorsList: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<SearchDoctorsHolder>() {

    /**
     * Creates a new ViewHolder for displaying a doctor item.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new instance of SearchDoctorsHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchDoctorsHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_item_doctor, parent, false)
        return SearchDoctorsHolder(view, onDoctorClick)
    }

    /**
     * Binds the data from a doctor to the corresponding ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the doctor in the list.
     */
    override fun onBindViewHolder(holder: SearchDoctorsHolder, position: Int) {
        holder.bind(doctorsList[position])
    }

    /**
     * Returns the total number of doctors in the list.
     *
     * @return The size of the doctors list.
     */
    override fun getItemCount(): Int = doctorsList.size

    /**
     * Updates the list of doctors dynamically using DiffUtil.
     *
     * @param newList The new list of doctors.
     */
    fun setDoctorsList(newList: List<Doctor>) {
        val diffResult = DiffUtil.calculateDiff(DoctorsDiffCallback(doctorsList, newList))
        doctorsList = newList
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
         * Checks if two items represent the same doctor by comparing their unique IDs.
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
