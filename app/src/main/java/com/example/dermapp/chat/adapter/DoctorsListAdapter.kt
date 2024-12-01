package com.example.dermapp.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.chat.holder.DoctorsListHolder
import com.example.dermapp.database.Doctor

class DoctorsListAdapter(
    private val context: Context,
    private var doctorsList: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorsListHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorsListHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_doctors_list_item, parent, false)
        return DoctorsListHolder(view, context, onDoctorClick)
    }

    override fun onBindViewHolder(holder: DoctorsListHolder, position: Int) {
        holder.bind(doctorsList[position])
    }

    override fun getItemCount(): Int = doctorsList.size

    fun updateDoctorsList(newDoctors: List<Doctor>) {
        val diffResult = DiffUtil.calculateDiff(DoctorsDiffCallback(doctorsList, newDoctors))
        doctorsList = newDoctors
        diffResult.dispatchUpdatesTo(this)
    }

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
