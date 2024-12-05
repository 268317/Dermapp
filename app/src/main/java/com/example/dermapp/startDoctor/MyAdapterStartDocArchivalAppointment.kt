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
 * RecyclerView Adapter for displaying archival appointments in the doctor's dashboard.
 *
 * This adapter is used to display a list of past appointments in a RecyclerView.
 * It allows the user to view details or edit archival appointments.
 *
 * @property appointmentsList A mutable list of [Appointment] objects to be displayed in the RecyclerView.
 * @property context The context of the activity or fragment that uses this adapter.
 */
class MyAdapterStartDocArchivalAppointment(
    private var appointmentsList: MutableList<Appointment>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartDocArchivalAppointment>() {

    private val firestore = FirebaseFirestore.getInstance()

    // Formatter for displaying date and time in the "dd.MM.yyyy HH:mm" format, localized to Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    /**
     * Inflates the layout for a single archival appointment item and creates a ViewHolder.
     *
     * @param parent The parent ViewGroup into which the new View will be added.
     * @param viewType The type of the new View (not used in this implementation).
     * @return A new [MyViewHolderStartDocArchivalAppointment] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocArchivalAppointment {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_doc_archival_appointment_view, parent, false)
        return MyViewHolderStartDocArchivalAppointment(view)
    }

    /**
     * Binds data from an archival appointment to a ViewHolder for display.
     *
     * @param holder The ViewHolder to which the data will be bound.
     * @param position The position of the appointment in the list.
     */
    override fun onBindViewHolder(holder: MyViewHolderStartDocArchivalAppointment, position: Int) {
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

        // Set up the button for editing appointment details
        holder.editButton.setOnClickListener {
            editAppointment(appointment)
        }
    }

    /**
     * Returns the total number of archival appointments in the list.
     *
     * @return The number of items in [appointmentsList].
     */
    override fun getItemCount(): Int {
        return appointmentsList.size
    }

    /**
     * Updates the adapter with a new list of archival appointments.
     *
     * @param newAppointments The updated list of [Appointment] objects to display.
     */
    fun updateAppointments(newAppointments: List<Appointment>) {
        appointmentsList.clear()
        appointmentsList.addAll(newAppointments)
        notifyDataSetChanged()
    }

    /**
     * Navigates to a screen to edit appointment details.
     *
     * This method launches the [CreateAppointmentDetailsDocActivity] to allow editing
     * the details of the selected archival appointment.
     *
     * @param appointment The [Appointment] object to edit.
     */
    private fun editAppointment(appointment: Appointment) {
        val intent = Intent(context, CreateAppointmentDetailsDocActivity::class.java)
        intent.putExtra("appointmentId", appointment.appointmentId)
        context.startActivity(intent)
    }
}
