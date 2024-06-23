package com.example.dermapp.startDoctor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.MedicalReport

class MyAdapterStartDocReport (private val reportsList: List<MedicalReport>) : RecyclerView.Adapter<MyViewHolderStartDocReport>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocReport {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_start_doc_reports_view, parent, false)
        return MyViewHolderStartDocReport(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderStartDocReport, position: Int) {
        val report = reportsList[position]
        // wyciągnąć imię i nazwisko lekarza
        holder.firstNamePat.text = report.doctorId
        holder.lastNamePat.text = report.patientPesel
        holder.reportDate.text = report.date.toString()
    }

    override fun getItemCount(): Int {
        return reportsList.size
    }
}
