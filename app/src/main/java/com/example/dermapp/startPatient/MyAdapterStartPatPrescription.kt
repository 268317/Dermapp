package com.example.dermapp.startPatient

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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

/**
 * Adapter for displaying prescriptions in a RecyclerView for a patient.
 *
 * @param prescriptionsList List of prescriptions to display
 * @param context Context used for starting activities and dialogs
 */
class MyAdapterStartPatPrescription(
    private var prescriptionsList: MutableList<Prescription>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartPatPrescription>() {

    // Firebase Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    // SimpleDateFormat configured for date and time
    private val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    /**
     * Creates and returns a new ViewHolder for prescription items.
     *
     * @param parent The parent ViewGroup into which the new View will be added after it is bound.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder instance for displaying prescriptions.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderStartPatPrescription {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_start_patient_prescription_view, parent, false)
        return MyViewHolderStartPatPrescription(view)
    }

    /**
     * Binds data to the ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item within the dataset.
     */
    override fun onBindViewHolder(holder: MyViewHolderStartPatPrescription, position: Int) {
        val prescription = prescriptionsList[position]

        // Fetch doctor details using coroutine
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val querySnapshot = firestore.collection("doctors")
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

        // Bind prescription data to ViewHolder
        prescription.date.let { prescriptionDate ->
            val formattedDateTime = dateTimeFormatter.format(prescriptionDate)
            holder.prescriptionDate.text = formattedDateTime
        }

        prescription.prescriptionText.let { prescriptionText ->
            holder.prescriptionText.text = prescriptionText
        }

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(prescription)
        }
    }

    /**
     * Returns the total number of prescriptions in the list.
     *
     * @return The size of the prescriptions list.
     */
    override fun getItemCount(): Int {
        return prescriptionsList.size
    }

    /**
     * Updates the adapter with new prescription data.
     *
     * @param newPrescriptions New list of prescriptions to display.
     */
    fun updatePrescriptions(newPrescriptions: List<Prescription>) {
        prescriptionsList.clear()
        prescriptionsList.addAll(newPrescriptions)
        notifyDataSetChanged()
    }

    /**
     * Shows a confirmation dialog for deleting a prescription.
     *
     * @param prescription Prescription object to delete.
     */
    private fun showDeleteConfirmationDialog(prescription: Prescription) {
        AlertDialog.Builder(context)
            .setTitle("Delete Prescription")
            .setMessage("Are you sure you want to delete this prescription?")
            .setPositiveButton("Delete") { dialog, _ ->
                deletePrescription(prescription)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Deletes a prescription from Firestore and updates the RecyclerView.
     *
     * @param prescription Prescription object to delete.
     */
    private fun deletePrescription(prescription: Prescription) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                // Check if prescriptionId is not empty
                if (prescription.prescriptionId.isNotEmpty()) {
                    // Delete the document from the "prescription" collection using the full path
                    firestore.collection("prescription")
                        .document(prescription.prescriptionId)
                        .delete()
                        .await()

                    // Remove the prescription from the local list and update the RecyclerView
                    prescriptionsList.remove(prescription)
                    notifyDataSetChanged()

                    Log.d("MyAdapter", "Prescription deleted successfully")
                } else {
                    Log.e("MyAdapter", "Prescription ID is empty or null")
                }

            } catch (e: Exception) {
                Log.e("MyAdapter", "Error deleting prescription: ${e.message}")
            }
        }
    }
}
