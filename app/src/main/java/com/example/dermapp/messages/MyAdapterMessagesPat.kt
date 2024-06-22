package com.example.dermapp.messages

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.database.Doctor
import com.example.dermapp.NewMessagePatActivity
import com.example.dermapp.R

class MyAdapterMessagesPat(private val context: Context, private var doctorsList: List<Doctor>) : RecyclerView.Adapter<MyViewHolderMessagesPat>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderMessagesPat {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.messages_pat_view_activity, parent, false)
        return MyViewHolderMessagesPat(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderMessagesPat, position: Int) {
        val doctor = doctorsList[position]
        holder.firstNameDoc.text = doctor.firstName
        holder.lastNameDoc.text = doctor.lastName
        holder.mailDoc.text = doctor.email
        holder.phoneDoc.text = doctor.phone

        // Set OnClickListener for the newMessageButtonPat
        holder.newMessageButtonPat.setOnClickListener {
            val intent = Intent(context, NewMessagePatActivity::class.java)
            intent.putExtra("doctorId", doctor.doctorId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return doctorsList.size
    }

    fun setDoctorsList(doctors: List<Doctor>) {
        this.doctorsList = doctors
        notifyDataSetChanged()
    }
}
