package com.example.dermapp.startDoctor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.AppointmentDetailsDocActivity
import com.example.dermapp.AppointmentDetailsPatActivity
import com.example.dermapp.CreateAppointmentDetailsDocActivity
import com.example.dermapp.R
import com.example.dermapp.database.Appointment
import com.example.dermapp.startPatient.MyViewHolderStartPatAppointment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MyAdapterStartDocArchivalAppointment(
    private var appointmentsList: MutableList<Appointment>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartDocArchivalAppointment>() {

    private val firestore = FirebaseFirestore.getInstance()

    // SimpleDateFormat configured for date and time in Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocArchivalAppointment {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_doc_archival_appointment_view, parent, false)
        return MyViewHolderStartDocArchivalAppointment(view)
    }


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
        holder.editButton.setOnClickListener {
            editAppointment(appointment)
        }
    }

    override fun getItemCount(): Int {
        return appointmentsList.size
    }

    fun updateAppointments(newAppointments: List<Appointment>) {
        appointmentsList.clear()
        appointmentsList.addAll(newAppointments)
        notifyDataSetChanged()
    }


    // Function to delete appointment from Firestore
    private fun editAppointment(appointment: Appointment) {
        val intent = Intent(context, CreateAppointmentDetailsDocActivity::class.java)
        intent.putExtra("appointmentId", appointment.appointmentId)
        context.startActivity(intent)
    }
}
