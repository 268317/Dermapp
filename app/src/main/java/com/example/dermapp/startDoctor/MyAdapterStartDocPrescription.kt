package com.example.dermapp.startDoctor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Prescription

class MyAdapterStartDocPrescription (private val prescriptionsList: List<Prescription>) : RecyclerView.Adapter<MyViewHolderStartDocPrescription>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocPrescription {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_start_doc_prescription_view, parent, false)
        return MyViewHolderStartDocPrescription(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderStartDocPrescription, position: Int) {
        val prescription = prescriptionsList[position]
        // wyciągnąć imię i nazwisko lekarza
        holder.firstNamePat.text = prescription.doctorId
        holder.lastNamePat.text = prescription.patientId
        holder.prescriptionDate.text = prescription.date.toString()
    }

    override fun getItemCount(): Int {
        return prescriptionsList.size
    }
}
