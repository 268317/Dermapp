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

class MyAdapterStartPatPrescription(
    private var prescriptionsList: MutableList<Prescription>,
    private val context: Context
) : RecyclerView.Adapter<MyViewHolderStartPatPrescription>() {

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

    override fun getItemCount(): Int {
        return prescriptionsList.size
    }

    // Update adapter with new data
    fun updatePrescriptions(newPrescriptions: List<Prescription>) {
        prescriptionsList.clear()
        prescriptionsList.addAll(newPrescriptions)
        notifyDataSetChanged()
    }

    // Function to show delete confirmation dialog
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

    // Function to delete prescription from Firestore
    private fun deletePrescription(prescription: Prescription) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                // Sprawdź czy prescriptionId nie jest puste
                if (prescription.prescriptionId.isNotEmpty()) {
                    // Usuń dokument z kolekcji "prescription" używając pełnej ścieżki
                    firestore.collection("prescription")
                        .document(prescription.prescriptionId)
                        .delete()
                        .await()

                    // Usuń receptę z lokalnej listy i zaktualizuj RecyclerView
                    prescriptionsList.remove(prescription)
                    notifyDataSetChanged()

                    Log.d("MyAdapter", "Prescription deleted successfully")
                } else {
                    Log.e("MyAdapter", "Prescription ID is empty or null")
                    // Obsłuż przypadek, gdy prescriptionId jest puste
                }

            } catch (e: Exception) {
                Log.e("MyAdapter", "Error deleting prescription: ${e.message}")
                // Obsłuż błąd usuwania recepty
            }
        }
    }



}