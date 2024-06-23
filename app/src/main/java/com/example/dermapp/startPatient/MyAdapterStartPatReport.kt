package com.example.dermapp.startPatient

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.ReportActivity
import com.example.dermapp.database.MedicalReport
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MyAdapterStartPatReport(
    private var reportsList: MutableList<MedicalReport>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartPatReport>() {

    private val firestore = FirebaseFirestore.getInstance()

    // SimpleDateFormat configured for date and time in Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartPatReport {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_patient_reports_view, parent, false)
        return MyViewHolderStartPatReport(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderStartPatReport, position: Int) {
        val report = reportsList[position]

        // Fetch doctor details using coroutine
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = firestore.collection("doctors")
                    .whereEqualTo("doctorId", report.doctorId)
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
            val intent = Intent(context, ReportActivity::class.java)
            intent.putExtra(ReportActivity.MEDICAL_REPORT_ID_EXTRA, report.medicalReportId)
            context.startActivity(intent)
        }

        // Set appointment date and time
        report.date.let { reportDate ->
            //val formattedDateTime = dateTimeFormatter.format(reportDate)
            holder.reportDate.text = report.date//formattedDateTime
        }
    }

    override fun getItemCount(): Int {
        return reportsList.size
    }

    // Update adapter with new data
    fun updateReports(newReport: List<MedicalReport>) {
        reportsList.clear()
        reportsList.addAll(newReport)
        notifyDataSetChanged()
    }
}
