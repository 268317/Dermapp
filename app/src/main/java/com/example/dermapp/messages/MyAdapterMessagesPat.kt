package com.example.dermapp.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Doctor

class MyAdapterMessagesPat (private val doctorsList: List<Doctor>) : RecyclerView.Adapter<MyViewHolderMessagesPat>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderMessagesPat {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.messages_pat_view_activity, parent, false)
        return MyViewHolderMessagesPat(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderMessagesPat, position: Int) {
        val doctor = doctorsList[position]
        holder.firstNameDoc.text = doctor.firstName
        holder.lastNameDoc.text = doctor.lastName
        holder.idDoc.text = doctor.doctorId
        holder.mailDoc.text = doctor.email
        holder.addressDoc.text = doctor.address
        holder.phoneDoc.text = doctor.phone
    }

    override fun getItemCount(): Int {
        return doctorsList.size
    }
}
