package com.example.dermapp.startDoctor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.AppointmentDetailsPatActivity
import com.example.dermapp.R
import com.example.dermapp.database.Prescription
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * RecyclerView Adapter for displaying prescriptions in the doctor's dashboard.
 *
 * This adapter is used to populate a list of prescriptions in a RecyclerView.
 * It provides functionalities for viewing detailed information about each prescription.
 *
 * @property prescriptionsList A mutable list of [Prescription] objects to be displayed in the RecyclerView.
 * @property context The context of the activity or fragment that uses this adapter.
 */
class MyAdapterStartDocPrescription(
    private var prescriptionsList: MutableList<Prescription>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartDocPrescription>() {

    private val firestore = FirebaseFirestore.getInstance()

    // Formatter for displaying date and time in the "dd.MM.yyyy HH:mm" format
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    /**
     * Inflates the layout for a single prescription item and creates a ViewHolder.
     *
     * @param parent The parent ViewGroup into which the new View will be added.
     * @param viewType The type of the new View (not used in this implementation).
     * @return A new [MyViewHolderStartDocPrescription] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocPrescription {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_doc_prescription_view, parent, false)
        return MyViewHolderStartDocPrescription(view)
    }

    /**
     * Binds data from a prescription to a ViewHolder for display.
     *
     * @param holder The ViewHolder to which the data will be bound.
     * @param position The position of the prescription in the list.
     */
    override fun onBindViewHolder(holder: MyViewHolderStartDocPrescription, position: Int) {
        val prescription = prescriptionsList[position]

        // Retrieve patient details using Firestore and populate the holder
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = firestore.collection("patients")
                    .whereEqualTo("userId", prescription.patientId)
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

        // Display formatted prescription date
        prescription.date.let { prescriptionDate ->
            val formattedDateTime = dateTimeFormatter.format(prescriptionDate)
            holder.prescriptionDate.text = formattedDateTime
        }

        // Handle details button click to view prescription details
        holder.detailsButton.setOnClickListener {
            val intent = Intent(context, AppointmentDetailsPatActivity::class.java)
            intent.putExtra("prescriptionId", prescription.prescriptionId)
            context.startActivity(intent)
        }
    }

    /**
     * Returns the total number of prescriptions in the list.
     *
     * @return The number of items in [prescriptionsList].
     */
    override fun getItemCount(): Int {
        return prescriptionsList.size
    }

    /**
     * Updates the adapter with a new list of prescriptions.
     *
     * @param newPrescriptions The updated list of [Prescription] objects to display.
     */
    fun updatePrescriptions(newPrescriptions: List<Prescription>) {
        prescriptionsList.clear()
        prescriptionsList.addAll(newPrescriptions)
        notifyDataSetChanged()
    }
}
