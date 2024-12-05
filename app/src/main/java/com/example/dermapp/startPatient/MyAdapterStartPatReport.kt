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

/**
 * Adapter for displaying medical reports in a RecyclerView for a patient.
 *
 * @param reportsList List of medical reports to display.
 * @param context Context used for starting activities.
 */
class MyAdapterStartPatReport(
    private var reportsList: MutableList<MedicalReport>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartPatReport>() {

    // Firebase Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    // SimpleDateFormat configured for date and time in Warsaw timezone
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Warsaw")
    }

    /**
     * Creates and returns a new ViewHolder for medical report items.
     *
     * @param parent The parent ViewGroup into which the new View will be added after it is bound.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder instance for displaying medical reports.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartPatReport {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_patient_reports_view, parent, false)
        return MyViewHolderStartPatReport(view)
    }

    /**
     * Binds data to the ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item within the dataset.
     */
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
                    val name = "${doctorDocument.getString("firstName") ?: ""} ${doctorDocument.getString("lastName") ?: ""}".trim()

                    // Update ViewHolder with doctor's name
                    holder.firstNameDoc.text = name
                } else {
                    // Handle case where no matching doctor document is found
                    holder.firstNameDoc.text = "Unknown Doctor"
                }
            } catch (e: Exception) {
                // Handle Firestore fetch errors
                holder.firstNameDoc.text = "Unknown Doctor"
            }
        }

        // Set click listener for viewing details of the report
        holder.seeDetailsButton.setOnClickListener {
            val intent = Intent(context, ReportActivity::class.java)
            intent.putExtra(ReportActivity.MEDICAL_REPORT_ID_EXTRA, report.medicalReportId)
            context.startActivity(intent)
        }

        // Set appointment date and time on the ViewHolder
        report.date.let { reportDate ->
            //val formattedDateTime = dateTimeFormatter.format(reportDate)
            holder.reportDate.text = report.date//formattedDateTime
        }
    }

    /**
     * Returns the total number of medical reports in the list.
     *
     * @return The size of the medical reports list.
     */
    override fun getItemCount(): Int {
        return reportsList.size
    }

    /**
     * Updates the adapter with new medical report data.
     *
     * @param newReport New list of medical reports to display.
     */
    fun updateReports(newReport: List<MedicalReport>) {
        reportsList.clear()
        reportsList.addAll(newReport)
        notifyDataSetChanged()
    }
}
