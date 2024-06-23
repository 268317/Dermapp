package com.example.dermapp.startPatient

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Prescription
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class MyAdapterStartPatPrescription(private var prescriptionsList: MutableList<Prescription>, private val context: Context) :
    RecyclerView.Adapter<MyViewHolderStartPatPrescription>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartPatPrescription {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_patient_prescription_view, parent, false)
        return MyViewHolderStartPatPrescription(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderStartPatPrescription, position: Int) {
        val prescription = prescriptionsList[position]

        // Fetch doctor details using coroutine
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = firestore.collection("prescription")
                    .whereEqualTo("doctorId", prescription.doctorId)
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

        prescription.date.let { prescriptionDate ->
            val formattedDateTime = dateTimeFormatter.format(prescriptionDate)
            holder.prescriptionDate.text = formattedDateTime
        }
    }

    override fun getItemCount(): Int {
        return prescriptionsList.size
    }

    // Update adapter with new data
    fun updatePrescriptions(newPrescription: List<Prescription>) {
        prescriptionsList.clear()
        prescriptionsList.addAll(newPrescription)
        notifyDataSetChanged()
    }
}