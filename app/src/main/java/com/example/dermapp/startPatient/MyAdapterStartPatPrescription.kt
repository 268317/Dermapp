package com.example.dermapp.startPatient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Appointment
import com.example.dermapp.database.Prescription

class MyAdapterStartPatPrescription (private val prescriptionsList: List<Prescription>) : RecyclerView.Adapter<MyViewHolderStartPatPrescription>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartPatPrescription {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_start_patient_prescription_view, parent, false)
        return MyViewHolderStartPatPrescription(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderStartPatPrescription, position: Int) {
        val prescription = prescriptionsList[position]
        // wyciągnąć imię i nazwisko lekarza
        holder.firstNameDoc.text = prescription.doctorId
        holder.lastNameDoc.text = prescription.patientPesel
        holder.prescriptionDate.text = prescription.prescriptionDate
    }

    override fun getItemCount(): Int {
        return prescriptionsList.size
    }
}
