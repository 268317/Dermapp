package com.example.dermapp.startPatient

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.AppointmentDetailsPatActivity
import com.example.dermapp.R
import com.example.dermapp.database.Appointment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Adapter for displaying archival appointments in a RecyclerView for a patient.
 *
 * @param appointmentsList List of appointments to display
 * @param context Context used for starting activities and dialogs
 */
class MyAdapterStartPatArchivalAppointment(
    private var appointmentsList: MutableList<Appointment>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartPatArchivalAppointment>() {

    // Firebase Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    // SimpleDateFormat configured for date and time in Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    /**
     * Creates and returns a new ViewHolder for appointment items.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartPatArchivalAppointment {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_patient_archival_appointment_view, parent, false)
        return MyViewHolderStartPatArchivalAppointment(view)
    }

    /**
     * Binds data to the ViewHolder.
     */
    override fun onBindViewHolder(holder: MyViewHolderStartPatArchivalAppointment, position: Int) {
        val appointment = appointmentsList[position]

        // Fetch doctor details using coroutine
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = firestore.collection("doctors")
                    .whereEqualTo("doctorId", appointment.doctorId)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val doctorDocument = querySnapshot.documents[0] // Assuming there's only one matching document
                    val name = "${doctorDocument.getString("firstName") ?: ""} ${doctorDocument.getString("lastName") ?: ""}".trim()

                    // Update ViewHolder with doctor's name
                    holder.firstNameDoc.text = name
                } else {
                    // Handle case where no matching doctor document is found
                    holder.firstNameDoc.text = "Unknown Doctor"
                }
            } catch (e: Exception) {
                // Handle Firestore fetch errors
                holder.firstNameDoc.text = "Unknown Doctor"
            }
        }

        // Handle click on see details button
        holder.seeDetailsButton.setOnClickListener {
            val intent = Intent(context, AppointmentDetailsPatActivity::class.java)
            intent.putExtra("appointmentId", appointment.appointmentId)
            context.startActivity(intent)
        }

        // Set appointment date and time
        appointment.datetime?.let { appointmentDate ->
            val formattedDateTime = dateTimeFormatter.format(appointmentDate)
            holder.appointmentDate.text = formattedDateTime
        }

        appointment.localization.let { appointmentLocalization ->
            holder.appointmentLoc.text = appointmentLocalization
        }

    }

    /**
     * Returns the total number of appointments in the list.
     */

    override fun getItemCount(): Int {
        return appointmentsList.size
    }

    /**
     * Updates the adapter with new appointment data.
     *
     * @param newAppointments New list of appointments to display
     */
    fun updateAppointments(newAppointments: List<Appointment>) {
        appointmentsList.clear()
        appointmentsList.addAll(newAppointments)
        notifyDataSetChanged()
    }

}
