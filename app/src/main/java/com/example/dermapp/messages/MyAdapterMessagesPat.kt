package com.example.dermapp.messages

import com.example.dermapp.database.Doctor
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.NewMessagePatActivity
import com.example.dermapp.R

class MyAdapterMessagesPat(private val context: Context, private val doctorsList: List<Doctor>) : RecyclerView.Adapter<MyViewHolderMessagesPat>() {
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

        holder.newMessageButtonPat.setOnClickListener {
            goToNewMessage()
        }
    }

    override fun getItemCount(): Int {
        return doctorsList.size
    }

    private fun goToNewMessage() {
        val intent = Intent(context, NewMessagePatActivity::class.java)
        context.startActivity(intent)
    }
}
