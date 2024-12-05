package com.example.dermapp.startPatient

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Adapter for displaying upcoming appointments in a RecyclerView for a patient.
 *
 * @param appointmentsList List of appointments to display
 * @param context Context used for starting activities and dialogs
 */
class MyAdapterStartPatAppointment(
    private var appointmentsList: MutableList<Appointment>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartPatAppointment>() {

    // Firebase Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    // SimpleDateFormat configured for date and time in Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    /**
     * Creates and returns a new ViewHolder for appointment items.
     *
     * @param parent The parent ViewGroup into which the new View will be added after it is bound.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder instance for displaying appointments.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartPatAppointment {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_patient_upcoming_appointment_view, parent, false)
        return MyViewHolderStartPatAppointment(view)
    }

    /**
     * Binds data to the ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item within the dataset.
     */
    override fun onBindViewHolder(holder: MyViewHolderStartPatAppointment, position: Int) {
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

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(appointment)
        }
    }

    /**
     * Returns the total number of appointments in the list.
     *
     * @return The size of the appointments list.
     */
    override fun getItemCount(): Int {
        return appointmentsList.size
    }

    /**
     * Updates the adapter with new appointment data.
     *
     * @param newAppointments New list of appointments to display.
     */
    fun updateAppointments(newAppointments: List<Appointment>) {
        appointmentsList.clear()
        appointmentsList.addAll(newAppointments)
        notifyDataSetChanged()
    }

    /**
     * Shows a confirmation dialog for deleting an appointment.
     *
     * @param appointment The appointment to delete.
     */
    private fun showDeleteConfirmationDialog(appointment: Appointment) {
        AlertDialog.Builder(context)
            .setTitle("Delete Appointment")
            .setMessage("Are you sure you want to delete this appointment?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteAppointment(appointment)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Deletes an appointment from Firestore and updates the RecyclerView.
     *
     * @param appointment The appointment to delete.
     */
    private fun deleteAppointment(appointment: Appointment) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                Log.d("delete appointment", appointment.datetime.toString())

                val startRange = Date((appointment.datetime?.time ?: System.currentTimeMillis()) - 1000) // 1 second earlier
                val endRange = Date((appointment.datetime?.time ?: System.currentTimeMillis()) + 1000)   // 1 second later

                val querySnapshot = firestore.collection("availableDates")
                    .whereGreaterThanOrEqualTo("datetime", startRange)
                    .whereLessThanOrEqualTo("datetime", endRange)
                    .whereEqualTo("doctorId", appointment.doctorId)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    Log.d("delete appointment", "OK")
                    val availableDataDoc = querySnapshot.documents[0]
                    val availableDataDocId = availableDataDoc.id

                    // Update isAvailable field to true in availableDates
                    firestore.collection("availableDates")
                        .document(availableDataDocId)
                        .update("isAvailable", true)
                        .await()
                }

                // Delete appointment document from the appointment collection
                firestore.collection("appointment")
                    .document(appointment.appointmentId)
                    .delete()
                    .await()

                // Update RecyclerView immediately after deletion
                updateAppointments(appointmentsList.filter { it.appointmentId != appointment.appointmentId })

            } catch (e: Exception) {
                // Handle error while deleting appointment
            }
        }
    }
}

