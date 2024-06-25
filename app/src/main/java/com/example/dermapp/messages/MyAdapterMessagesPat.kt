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

    /**
     * Adapter for populating a RecyclerView with a list of doctors for patient messages.
     *
     * @param context The context of the application or activity.
     * @param doctorsList The list of Doctor objects to be displayed.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderMessagesPat {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.messages_pat_view_activity, parent, false)
        return MyViewHolderMessagesPat(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
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

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in the data set.
     */
    override fun getItemCount(): Int {
        return doctorsList.size
    }

    /**
     * Update the list of doctors displayed by the adapter.
     *
     * @param doctors The new list of Doctor objects to be displayed.
     */
    fun setDoctorsList(doctors: List<Doctor>) {
        this.doctorsList = doctors
        notifyDataSetChanged()
    }
}
