package com.example.dermapp.startPatient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.MedicalReport

class MyAdapterStartPatReport (private val reportsList: List<MedicalReport>) : RecyclerView.Adapter<MyViewHolderStartPatReport>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartPatReport {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_start_patient_reports_view, parent, false)
        return MyViewHolderStartPatReport(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderStartPatReport, position: Int) {
        val report = reportsList[position]
        // wyciągnąć imię i nazwisko lekarza
        holder.firstNameDoc.text = report.doctorId
        holder.lastNameDoc.text = report.patientPesel
        holder.reportDate.text = report.reportDate.toString()
    }

    override fun getItemCount(): Int {
        return reportsList.size
    }
}
