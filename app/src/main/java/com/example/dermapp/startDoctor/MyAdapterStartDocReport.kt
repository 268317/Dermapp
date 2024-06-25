package com.example.dermapp.startDoctor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.ReportDocActivity
import com.example.dermapp.database.MedicalReport
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * RecyclerView Adapter for displaying medical reports in StartDocActivity.
 * @param reportsList List of MedicalReport objects to display.
 * @param context Context of the activity or fragment using this adapter.
 */
class MyAdapterStartDocReport(
    private var reportsList: MutableList<MedicalReport>,
    private val context: Context
    ) : RecyclerView.Adapter<MyViewHolderStartDocReport>() {

    private val firestore = FirebaseFirestore.getInstance()

//    // SimpleDateFormat configured for date and time in Warsaw timezone
//    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
//        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
//    }

    /**
     * Creates a new ViewHolder by inflating the layout defined in R.layout.activity_start_doc_reports_view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocReport {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_doc_reports_view, parent, false)
        return MyViewHolderStartDocReport(view)
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     */
    override fun onBindViewHolder(holder: MyViewHolderStartDocReport, position: Int) {
        val report = reportsList[position]

        // Fetch patient details using coroutine
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = firestore.collection("patients")
                    .whereEqualTo("pesel", report.patientPesel)
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

        // Click listener to view full details of the medical report
        holder.seeDetailsButton.setOnClickListener {
            val intent = Intent(context, ReportDocActivity::class.java)
            intent.putExtra(ReportDocActivity.MEDICAL_REPORT_ID_EXTRA, report.medicalReportId)
            context.startActivity(intent)
        }

        // Set appointment date and time
        report.date.let { reportDate ->
            //val formattedDateTime = dateTimeFormatter.format(reportDate)
            holder.reportDate.text = report.date//formattedDateTime
        }
    }

    /**
     * Returns the number of items in the reportsList.
     */
    override fun getItemCount(): Int {
        return reportsList.size
    }

    /**
     * Updates the adapter with new data.
     * @param newReport List of updated MedicalReport objects.
     */
    fun updateReports(newReport: List<MedicalReport>) {
        reportsList.clear()
        reportsList.addAll(newReport)
        notifyDataSetChanged()
    }
}