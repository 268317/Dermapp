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

class MyAdapterStartPatAppointment(private var appointmentsList: MutableList<Appointment>, private val context: Context) :
    RecyclerView.Adapter<MyViewHolderStartPatAppointment>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartPatAppointment {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_patient_upcoming_appointment_view, parent, false)
        return MyViewHolderStartPatAppointment(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderStartPatAppointment, position: Int) {
        val appointment = appointmentsList[position]
<<<<<<< HEAD
        holder.firstNameDoc.text = appointment.doctorId
        holder.lastNameDoc.text = appointment.patientId
        holder.appointmentDate.text = appointment.datetime.toString()
=======
>>>>>>> parent of 14438d7 (RV update)

        // Fetch doctor details using coroutine
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = firestore.collection("doctors")
                    .whereEqualTo("doctorId", appointment.doctorId)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val doctorDocument = querySnapshot.documents[0] // Assuming there's only one matching document
                    val firstName = doctorDocument.getString("firstName") ?: ""
                    val lastName = doctorDocument.getString("lastName") ?: ""

                    // Update ViewHolder with doctor's name
                    holder.firstNameDoc.text = firstName
                    holder.lastNameDoc.text = lastName
                } else {
                    // Handle case where no matching doctor document is found
                    holder.firstNameDoc.text = "Unknown"
                    holder.lastNameDoc.text = "Doctor"
                }
            } catch (e: Exception) {
                // Handle Firestore fetch errors
                holder.firstNameDoc.text = "Unknown"
                holder.lastNameDoc.text = "Doctor"
            }
        }

        holder.seeDetailsButton.setOnClickListener {
            val intent = Intent(context, AppointmentDetailsPatActivity::class.java)
//            intent.putExtra("appointmentId", appointment.appointmentId)
            context.startActivity(intent)
        }

        // Set appointment date and time
        appointment.datetime?.let { appointmentDate ->
            val formattedDateTime = dateTimeFormatter.format(appointmentDate)
            holder.appointmentDate.text = formattedDateTime
        }

        // Handle delete button click (if needed)
        holder.deleteButton.setOnClickListener {
            // Handle deletion logic
            // Example: removeAppointment(holder.adapterPosition)
        }


    }

    override fun getItemCount(): Int {
        return appointmentsList.size
    }

    // Update adapter with new data
    fun updateAppointments(newAppointments: List<Appointment>) {
        appointmentsList.clear()
        appointmentsList.addAll(newAppointments)
        notifyDataSetChanged()
    }
}