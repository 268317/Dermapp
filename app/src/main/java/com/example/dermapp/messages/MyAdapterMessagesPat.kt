package com.example.dermapp.messages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.dermapp.NewMessagePatActivity
import com.example.dermapp.R
import com.example.dermapp.database.Doctor

class MyAdapterMessagesPat(private val context: Context, private var doctorsList: List<Doctor>) : RecyclerView.Adapter<MyViewHolderMessagesPat>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderMessagesPat {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_pat_view_activity, parent, false)
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

        // Use Glide to load the profile photo URL into imageDoc
        Glide.with(context)
            .load(doctor.profilePhoto) // Assuming doctor.profilePhoto contains the URL
            .apply(RequestOptions.bitmapTransform(CircleCrop())) // Make image circular
            .placeholder(R.drawable.black_account_circle) // Optional: Add a placeholder
            .error(R.drawable.black_account_circle) // Optional: Add an error image if URL fails
            .into(holder.imageDoc)

        // Set OnClickListener for the imageDoc
        holder.imageDoc.setOnClickListener {
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
    @SuppressLint("NotifyDataSetChanged")
    fun setDoctorsList(doctors: List<Doctor>) {
        this.doctorsList = doctors
        notifyDataSetChanged()
    }
}
