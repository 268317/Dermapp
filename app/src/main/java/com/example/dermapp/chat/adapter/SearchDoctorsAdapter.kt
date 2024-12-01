package com.example.dermapp.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.chat.holder.SearchDoctorsHolder
import com.example.dermapp.database.Doctor

class SearchDoctorsAdapter(
    private val context: Context,
    private var doctorsList: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<SearchDoctorsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchDoctorsHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_item_doctor, parent, false)
        return SearchDoctorsHolder(view, onDoctorClick)
    }

    override fun onBindViewHolder(holder: SearchDoctorsHolder, position: Int) {
        holder.bind(doctorsList[position])
    }

    override fun getItemCount(): Int = doctorsList.size

    /**
     * Updates the list of doctors dynamically using DiffUtil.
     */
    fun setDoctorsList(newList: List<Doctor>) {
        val diffResult = DiffUtil.calculateDiff(DoctorsDiffCallback(doctorsList, newList))
        doctorsList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * DiffUtil Callback for efficiently updating the RecyclerView.
     */
    class DoctorsDiffCallback(
        private val oldList: List<Doctor>,
        private val newList: List<Doctor>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].doctorId == newList[newItemPosition].doctorId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
