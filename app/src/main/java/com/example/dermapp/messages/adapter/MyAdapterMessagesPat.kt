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
import com.example.dermapp.database.Doctor
import com.example.dermapp.messages.NewMessagePatActivity
import com.example.dermapp.messages.holder.MyViewHolderMessagesPat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyAdapterMessagesPat(private val context: Context, private var doctorsList: List<Doctor>) :
    RecyclerView.Adapter<MyViewHolderMessagesPat>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderMessagesPat {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_pat_view_activity, parent, false)
        return MyViewHolderMessagesPat(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderMessagesPat, position: Int) {
        val doctor = doctorsList[position]
        holder.firstNameDoc.text = doctor.firstName
        holder.lastNameDoc.text = doctor.lastName

        Glide.with(context)
            .load(doctor.profilePhoto)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.black_account_circle)
            .error(R.drawable.black_account_circle)
            .into(holder.imageDoc)

        val statusDrawable = if (doctor.isOnline) {
            R.drawable.status_indicator_background_online
        } else {
            R.drawable.status_indicator_background_offline
        }
        holder.statusIndicatorPat.setBackgroundResource(statusDrawable)

        holder.imageDoc.setOnClickListener {
            val currentPatientId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentPatientId != null) {
                val conversationId = generateConversationId(doctor.doctorId, currentPatientId)
                ensureConversationExists(doctor.doctorId, currentPatientId) {
                    val intent = Intent(context, NewMessagePatActivity::class.java)
                    intent.putExtra("doctorId", doctor.doctorId)
                    intent.putExtra("conversationId", conversationId)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = doctorsList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setDoctorsList(doctors: List<Doctor>) {
        this.doctorsList = doctors
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
