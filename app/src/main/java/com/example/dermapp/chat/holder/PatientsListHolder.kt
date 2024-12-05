package com.example.dermapp.chat.holder

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.dermapp.R
import com.example.dermapp.database.Patient

/**
 * ViewHolder for displaying a single patient's item in the RecyclerView.
 *
 * @param itemView The view of the individual item.
 * @param context The context where the ViewHolder is used.
 * @param onPatientClick A callback function invoked when the patient item is clicked.
 */
class PatientsListHolder(
    itemView: View,
    private val context: Context,
    private val onPatientClick: (Patient) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val profileImage: ImageView = itemView.findViewById(R.id.patientsListItemProfileImage)
    private val firstName: TextView = itemView.findViewById(R.id.patientsListItemFirstName)
    private val lastName: TextView = itemView.findViewById(R.id.patientsListItemLastName)
    private val statusIndicator: View = itemView.findViewById(R.id.patientsListItemStatus)

    /**
     * Binds the patient data to the ViewHolder and sets up UI components.
     *
     * @param patient The patient object containing data to display.
     */
    fun bind(patient: Patient) {

        // Debugging log to track the binding process
        Log.d("PatientsListHolder", "Patient: ${patient.firstName} ${patient.lastName}, isOnline: ${patient.isOnline}")

        // Setting the first name and last name
        firstName.text = patient.firstName
        lastName.text = patient.lastName

        // Loading the profile image using Glide with a placeholder and circular crop
        Glide.with(context)
            .load(patient.profilePhoto)
            .placeholder(R.drawable.black_account_circle)
            .error(R.drawable.black_account_circle)
            .transform(CircleCrop())
            .into(profileImage)

        // Setting the status indicator based on the patient's online status
        val statusDrawable = if (patient.isOnline) {
            R.drawable.status_indicator_background_online
        } else {
            R.drawable.status_indicator_background_offline
        }
        statusIndicator.setBackgroundResource(statusDrawable)

        // Setting up a click listener for the patient item
        itemView.setOnClickListener { onPatientClick(patient) }
    }
}
