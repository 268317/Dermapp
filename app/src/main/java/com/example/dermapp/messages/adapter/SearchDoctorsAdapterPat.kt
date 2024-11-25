package com.example.dermapp.messages.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Doctor

class SearchDoctorsAdapterPat(
    private val context: Context,
    private var doctorsList: MutableList<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<SearchDoctorsAdapterPat.DoctorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctorsList[position]
        holder.bind(doctor)
    }

    override fun getItemCount(): Int = doctorsList.size

    fun setDoctorsList(newList: MutableList<Doctor>) {
        doctorsList = newList
        notifyDataSetChanged()
    }

    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.doctorName)

        fun bind(doctor: Doctor) {
            nameTextView.text = "${doctor.firstName} ${doctor.lastName}"
            itemView.setOnClickListener { onDoctorClick(doctor) }
        }
    }
}
