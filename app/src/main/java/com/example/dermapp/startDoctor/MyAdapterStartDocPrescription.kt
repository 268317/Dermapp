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
 * RecyclerView Adapter for displaying prescriptions in StartDocActivity.
 * @param prescriptionsList List of Prescription objects to display.
 * @param context Context of the activity or fragment using this adapter.
 */
class MyAdapterStartDocPrescription(
    private var prescriptionsList: MutableList<Prescription>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartDocPrescription>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    /**
     * Creates a new ViewHolder by inflating the layout defined in R.layout.activity_start_doc_prescription_view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartDocPrescription {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_doc_prescription_view, parent, false)
        return MyViewHolderStartDocPrescription(view)
    }

    /**
     * Binds data to the ViewHolder at the specified position.
     */
    override fun onBindViewHolder(holder: MyViewHolderStartDocPrescription, position: Int) {
        val prescription = prescriptionsList[position]

        // Fetch doctor details using coroutine
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = firestore.collection("patients")
                    .whereEqualTo("userId", prescription.patientId)
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

        // Bind prescription data to ViewHolder
        prescription.date.let { prescriptionDate ->
            val formattedDateTime = dateTimeFormatter.format(prescriptionDate)
            holder.prescriptionDate.text = formattedDateTime
        }

        //prescription.prescriptionText.let { prescriptionText ->
          //  holder.prescriptionText.text = prescriptionText
        //}

        // Handle details button click to view prescription details
        holder.detailsButton.setOnClickListener {
            val intent = Intent(context, AppointmentDetailsPatActivity::class.java)
            intent.putExtra("prescriptionId", prescription.prescriptionId)
            context.startActivity(intent)
        }
    }

    /**
     * Returns the number of items in the prescriptionsList.
     */
    override fun getItemCount(): Int {
        return prescriptionsList.size
    }

    /**
     * Updates the adapter with new data.
     * @param newPrescriptions List of updated Prescription objects.
     */
    fun updatePrescriptions(newPrescriptions: List<Prescription>) {
        prescriptionsList.clear()
        prescriptionsList.addAll(newPrescriptions)
        notifyDataSetChanged()
    }


}
