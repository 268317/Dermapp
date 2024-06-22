import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.MedicalReport
import com.example.dermapp.startPatient.MyViewHolderStartPatReport

class MyAdapterStartPatReport(private val reportsList: List<MedicalReport>, private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<MyViewHolderStartPatReport>() {

    interface OnItemClickListener {
        fun onItemClick(medicalReportId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartPatReport {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_start_patient_reports_view, parent, false)
        return MyViewHolderStartPatReport(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderStartPatReport, position: Int) {
        val report = reportsList[position]
        holder.firstNameDoc.text = report.doctorId
        holder.lastNameDoc.text = report.patientPesel
        holder.reportDate.text = report.reportDate.toString()

        // Set onClickListener for "SEE FULL REPORT" button
        holder.buttonSeeFullReport.setOnClickListener {
            itemClickListener.onItemClick(report.medicalReportId)
        }
    }

    override fun getItemCount(): Int {
        return reportsList.size
    }
}