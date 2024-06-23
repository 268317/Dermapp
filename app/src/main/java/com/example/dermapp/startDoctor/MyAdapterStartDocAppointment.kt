package com.example.dermapp.startDoctor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Appointment

class MyAdapterStartDocAppointment (private val appointmentsList: List<Appointment>) : RecyclerView.Adapter<MyViewHolderStartDocAppointment>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocAppointment {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_start_doc_upcoming_appointment_view, parent, false)
        return MyViewHolderStartDocAppointment(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderStartDocAppointment, position: Int) {
        val appointment = appointmentsList[position]
        // wyciągnąć imię i nazwisko lekarza
        holder.firstNamePat.text = appointment.doctorId
//        holder.lastNamePat.text = appointment.patientPesel
        holder.appointmentDate.text = appointment.datetime.toString()
<<<<<<< HEAD
        holder.lastNamePat.text = appointment.patientId
        holder.appointmentDate.text = appointment.datetime.toString()
=======
>>>>>>> parent of 14438d7 (RV update)
    }

    override fun getItemCount(): Int {
        return appointmentsList.size
    }
}
