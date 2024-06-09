package com.example.dermapp.startPatient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Appointment

class MyAdapterStartPatAppointment (private val appointmentsList: List<Appointment>) : RecyclerView.Adapter<MyViewHolderStartPatAppointment>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartPatAppointment {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_start_patient_upcoming_appointment_view, parent, false)
        return MyViewHolderStartPatAppointment(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderStartPatAppointment, position: Int) {
        val appointment = appointmentsList[position]
        // wyciągnąć imię i nazwisko lekarza
        holder.firstNameDoc.text = appointment.doctorId
        holder.lastNameDoc.text = appointment.patientPesel
        holder.appointmentDate.text = appointment.appointmentDate.toString()
    }

    override fun getItemCount(): Int {
        return appointmentsList.size
    }
}
