package com.example.dermapp.startDoctor

import android.content.Context
import android.content.Intent
import android.util.Log
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
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * RecyclerView Adapter for displaying upcoming appointments in the doctor's dashboard.
 *
 * This adapter is used to populate a list of upcoming appointments in a RecyclerView.
 * It provides functionalities for viewing details, deleting appointments, and updating
 * the displayed list dynamically.
 *
 * @property appointmentsList A mutable list of [Appointment] objects to be displayed in the RecyclerView.
 * @property context The context of the activity or fragment that uses this adapter.
 */
class MyAdapterStartDocAppointment(
    private var appointmentsList: MutableList<Appointment>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartDocAppointment>() {

    private val firestore = FirebaseFirestore.getInstance()

    // Formatter for displaying date and time in the "dd.MM.yyyy HH:mm" format, localized to Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    /**
     * Inflates the layout for a single appointment item and creates a ViewHolder.
     *
     * @param parent The parent ViewGroup into which the new View will be added.
     * @param viewType The type of the new View (not used in this implementation).
     * @return A new [MyViewHolderStartDocAppointment] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocAppointment {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_doc_upcoming_appointment_view, parent, false)
        return MyViewHolderStartDocAppointment(view)
    }

    /**
     * Binds data from an appointment to a ViewHolder for display.
     *
     * @param holder The ViewHolder to which the data will be bound.
     * @param position The position of the appointment in the list.
     */
    override fun onBindViewHolder(holder: MyViewHolderStartDocAppointment, position: Int) {
        val appointment = appointmentsList[position]

        // Retrieve patient details using Firestore and populate the holder
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = firestore.collection("patients")
                    .whereEqualTo("userId", appointment.patientId)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val patientDocument = querySnapshot.documents[0]
                    val firstName = patientDocument.getString("firstName") ?: ""
                    val lastName = patientDocument.getString("lastName") ?: ""

                    holder.firstNamePat.text = "${firstName} ${lastName}"
                } else {
                    holder.firstNamePat.text = "Unknown Patient"
                }
            } catch (e: Exception) {
                holder.firstNamePat.text = "Unknown Patient"
            }
        }

        // Set up the button for viewing appointment details
        holder.seeDetailsButton.setOnClickListener {
            val intent = Intent(context, AppointmentDetailsDocActivity::class.java)
            intent.putExtra("appointmentId", appointment.appointmentId)
            context.startActivity(intent)
        }

        // Display formatted appointment date and time
        appointment.datetime?.let { appointmentDate ->
            val formattedDateTime = dateTimeFormatter.format(appointmentDate)
            holder.appointmentDate.text = formattedDateTime
        }

        // Display the appointment location
        appointment.localization.let { appointmentLocalization ->
            holder.appointmentLoc.text = appointmentLocalization
        }

        // Set up the delete button to show a confirmation dialog
        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(appointment)
        }
    }

    /**
     * Returns the total number of appointments in the list.
     *
     * @return The number of items in [appointmentsList].
     */
    override fun getItemCount(): Int {
        return appointmentsList.size
    }

    /**
     * Updates the adapter with a new list of appointments.
     *
     * @param newAppointments The updated list of [Appointment] objects to display.
     */
    fun updateAppointments(newAppointments: List<Appointment>) {
        appointmentsList.clear()
        appointmentsList.addAll(newAppointments)
        notifyDataSetChanged()
    }

    /**
     * Displays a confirmation dialog before deleting an appointment.
     *
     * @param appointment The appointment to be deleted.
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
     * Deletes an appointment from Firestore and updates the UI.
     *
     * This method removes the appointment from Firestore and updates the RecyclerView.
     *
     * @param appointment The [Appointment] object to be deleted.
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
                    val availableDateDoc = querySnapshot.documents[0]
                    val availableDateDocId = availableDateDoc.id

                    // Update the isAvailable field to true
                    firestore.collection("availableDates")
                        .document(availableDateDocId)
                        .update("isAvailable", true)
                        .await()
                }

                // Remove the appointment from the Firestore collection
                firestore.collection("appointment")
                    .document(appointment.appointmentId)
                    .delete()
                    .await()

                // Refresh the list of appointments in the RecyclerView
                updateAppointments(appointmentsList.filter { it.appointmentId != appointment.appointmentId })

            } catch (e: Exception) {
                // Handle errors during deletion
            }
        }
    }
}
