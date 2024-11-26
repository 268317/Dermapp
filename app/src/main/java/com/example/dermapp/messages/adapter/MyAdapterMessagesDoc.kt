package com.example.dermapp.messages.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.dermapp.R
import com.example.dermapp.database.Patient
import com.example.dermapp.messages.NewMessageDocActivity
import com.example.dermapp.messages.holder.MyViewHolderMessagesDoc
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyAdapterMessagesDoc(private val context: Context, private var patientsList: List<Patient>) :
    RecyclerView.Adapter<MyViewHolderMessagesDoc>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderMessagesDoc {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_doc_view_activity, parent, false)
        return MyViewHolderMessagesDoc(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderMessagesDoc, position: Int) {
        val patient = patientsList[position]
        holder.firstNamePat.text = patient.firstName
        holder.lastNamePat.text = patient.lastName

        Glide.with(context)
            .load(patient.profilePhoto)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.black_account_circle)
            .error(R.drawable.black_account_circle)
            .into(holder.imagePat)

        val statusDrawable = if (patient.isOnline) {
            R.drawable.status_indicator_background_online
        } else {
            R.drawable.status_indicator_background_offline
        }
        holder.statusIndicatorDoc.setBackgroundResource(statusDrawable)

        holder.imagePat.setOnClickListener {
            val currentDoctorId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentDoctorId != null) {
                val conversationId = generateConversationId(currentDoctorId, patient.appUserId)
                ensureConversationExists(currentDoctorId, patient.appUserId) {
                    val intent = Intent(context, NewMessageDocActivity::class.java)
                    intent.putExtra("patientId", patient.appUserId)
                    intent.putExtra("conversationId", conversationId)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = patientsList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setPatientsList(patients: List<Patient>) {
        this.patientsList = patients
        notifyDataSetChanged()
    }

    private fun generateConversationId(doctorId: String, patientId: String): String {
        return "$doctorId-$patientId"
    }

    private fun ensureConversationExists(
        doctorId: String,
        patientId: String,
        onComplete: (conversationId: String) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val conversationRef = firestore.collection("conversation")
        val conversationId = generateConversationId(doctorId, patientId)

        conversationRef.document(conversationId).get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val newConversation = mapOf(
                    "conversationId" to conversationId,
                    "doctorId" to doctorId,
                    "patientId" to patientId,
                    "lastMessage" to "",
                    "lastMessageTimestamp" to null
                )
                conversationRef.document(conversationId).set(newConversation).addOnSuccessListener {
                    onComplete(conversationId)
                }
            } else {
                onComplete(conversationId)
            }
        }
    }
}
