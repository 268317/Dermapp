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
 * RecyclerView Adapter for displaying medical reports in the doctor's dashboard.
 *
 * This adapter is used to populate a list of medical reports in a RecyclerView.
 * It allows the user to view detailed information about each medical report.
 *
 * @property reportsList A mutable list of [MedicalReport] objects to be displayed in the RecyclerView.
 * @property context The context of the activity or fragment that uses this adapter.
 */
class MyAdapterStartDocReport(
    private var reportsList: MutableList<MedicalReport>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartDocReport>() {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Inflates the layout for a single medical report item and creates a ViewHolder.
     *
     * @param parent The parent ViewGroup into which the new View will be added.
     * @param viewType The type of the new View (not used in this implementation).
     * @return A new [MyViewHolderStartDocReport] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocReport {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_doc_reports_view, parent, false)
        return MyViewHolderStartDocReport(view)
    }

    /**
     * Binds data from a medical report to a ViewHolder for display.
     *
     * @param holder The ViewHolder to which the data will be bound.
     * @param position The position of the medical report in the list.
     */
    override fun onBindViewHolder(holder: MyViewHolderStartDocReport, position: Int) {
        val report = reportsList[position]

        // Retrieve patient details using Firestore and populate the holder
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = firestore.collection("patients")
                    .whereEqualTo("pesel", report.patientPesel)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val patientDocument = querySnapshot.documents[0]
                    val firstName = patientDocument.getString("firstName") ?: ""
                    val lastName = patientDocument.getString("lastName") ?: ""

                    holder.firstNamePat.text = "${firstName} ${lastName}"
                } else {
                    holder.firstNamePat.text = "Unknown Patient"
                }
            } catch (e: Exception) {
                holder.firstNamePat.text = "Unknown Patient"
            }
        }

        // Set up a click listener to view detailed information about the medical report
        holder.seeDetailsButton.setOnClickListener {
            val intent = Intent(context, ReportDocActivity::class.java)
            intent.putExtra(ReportDocActivity.MEDICAL_REPORT_ID_EXTRA, report.medicalReportId)
            context.startActivity(intent)
        }

        // Display the date of the medical report
        report.date.let { reportDate ->
            holder.reportDate.text = reportDate
        }
    }

    /**
     * Returns the total number of medical reports in the list.
     *
     * @return The number of items in [reportsList].
     */
    override fun getItemCount(): Int {
        return reportsList.size
    }

    /**
     * Updates the adapter with a new list of medical reports.
     *
     * @param newReport The updated list of [MedicalReport] objects to display.
     */
    fun updateReports(newReport: List<MedicalReport>) {
        reportsList.clear()
        reportsList.addAll(newReport)
        notifyDataSetChanged()
    }
}
