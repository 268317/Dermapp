package com.example.dermapp.startDoctor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.AppointmentDetailsDocActivity
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
 * RecyclerView Adapter for displaying upcoming appointments in StartDocActivity.
 * @param appointmentsList List of Appointment objects to display.
 * @param context Context of the activity or fragment using this adapter.
 */
class MyAdapterStartDocAppointment(
    private var appointmentsList: MutableList<Appointment>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartDocAppointment>() {

    private val firestore = FirebaseFirestore.getInstance()

    // SimpleDateFormat configured for date and time in Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    /**
     * Creates a new ViewHolder by inflating the layout defined in R.layout.activity_start_doc_upcoming_appointment_view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocAppointment {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_doc_upcoming_appointment_view, parent, false)
        return MyViewHolderStartDocAppointment(view)
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     */
    override fun onBindViewHolder(holder: MyViewHolderStartDocAppointment, position: Int) {
        val appointment = appointmentsList[position]

        // Fetch patient details using coroutine
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

                    holder.firstNamePat.text = "${firstName} ${lastName}"
                } else {
                    holder.firstNamePat.text = "Unknown Patient"
                }
            } catch (e: Exception) {
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

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(appointment)
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
     * Function to show delete confirmation dialog.
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
     * Function to delete appointment from Firestore and update RecyclerView.
     * @param appointment The appointment to delete.
     */
    private fun deleteAppointment(appointment: Appointment) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                // Fetch corresponding availableData document
                val querySnapshot = firestore.collection("availableData")
                    .whereEqualTo("datetime", appointment.datetime)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    // Assuming there's only one document matching datetime
                    val availableDataDoc = querySnapshot.documents[0]
                    val availableDataDocId = availableDataDoc.id

                    // Update isAvailable field to true in availableData
                    firestore.collection("availableData")
                        .document(availableDataDocId)
                        .update("isAvailable", true)
                        .await()
                }

                // Delete appointment document from appointment collection
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
