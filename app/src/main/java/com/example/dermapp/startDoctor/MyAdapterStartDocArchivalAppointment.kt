package com.example.dermapp.startDoctor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.AppointmentDetailsDocActivity
import com.example.dermapp.CreateAppointmentDetailsDocActivity
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
 * RecyclerView Adapter for displaying archival appointments in StartDocActivity.
 * @param appointmentsList List of Appointment objects to display.
 * @param context Context of the activity or fragment using this adapter.
 */
class MyAdapterStartDocArchivalAppointment(
    private var appointmentsList: MutableList<Appointment>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartDocArchivalAppointment>() {

    private val firestore = FirebaseFirestore.getInstance()

    // SimpleDateFormat configured for date and time in Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    /**
     * Creates a new ViewHolder by inflating the layout defined in R.layout.activity_start_doc_archival_appointment_view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocArchivalAppointment {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_doc_archival_appointment_view, parent, false)
        return MyViewHolderStartDocArchivalAppointment(view)
    }


    /**
     * Binds data to the ViewHolder at the specified position.
     */
    override fun onBindViewHolder(holder: MyViewHolderStartDocArchivalAppointment, position: Int) {
        val appointment = appointmentsList[position]

        // Fetch doctor details using coroutine
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = firestore.collection("patients")
                    .whereEqualTo("userId", appointment.patientId)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val doctorDocument = querySnapshot.documents[0] // Assuming there's only one matching document
                    val firstName = doctorDocument.getString("firstName") ?: ""
                    val lastName = doctorDocument.getString("lastName") ?: ""

                    // Update ViewHolder with doctor's name
                    holder.firstNamePat.text = "${firstName} ${lastName}"
                } else {
                    // Handle case where no matching doctor document is found
                    holder.firstNamePat.text = "Unknown Patient"
                }
            } catch (e: Exception) {
                // Handle Firestore fetch errors
                holder.firstNamePat.text = "Unknown Patient"
            }
        }

        // Handle see details button click to view appointment details
        holder.seeDetailsButton.setOnClickListener {
            val intent = Intent(context, AppointmentDetailsDocActivity::class.java)
            intent.putExtra("appointmentId", appointment.appointmentId)
            context.startActivity(intent)
        }

        // Set appointment date and time
        appointment.datetime?.let { appointmentDate ->
            val formattedDateTime = dateTimeFormatter.format(appointmentDate)
            holder.appointmentDate.text = formattedDateTime
        }

        // Handle edit button click to edit appointment details
        holder.editButton.setOnClickListener {
            editAppointment(appointment)
        }
    }

    /**
     * Returns the number of items in the appointmentsList.
     */
    override fun getItemCount(): Int {
        return appointmentsList.size
    }

    /**
     * Updates the adapter with new data.
     * @param newAppointments List of updated Appointment objects.
     */
    fun updateAppointments(newAppointments: List<Appointment>) {
        appointmentsList.clear()
        appointmentsList.addAll(newAppointments)
        notifyDataSetChanged()
    }


    /**
     * Navigates to a screen to edit appointment details.
     * @param appointment The appointment to edit.
     */
    private fun editAppointment(appointment: Appointment) {
        val intent = Intent(context, CreateAppointmentDetailsDocActivity::class.java)
        intent.putExtra("appointmentId", appointment.appointmentId)
        context.startActivity(intent)
    }
}
