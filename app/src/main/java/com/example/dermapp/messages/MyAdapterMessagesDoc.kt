package com.example.dermapp.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Patient


class MyAdapterMessagesDoc (private val patientList: List<Patient>) : RecyclerView.Adapter<MyViewHolderMessagesDoc>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderMessagesDoc {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.messages_doc_view_activity, parent, false)
        return MyViewHolderMessagesDoc(view)
    }

    override fun onBindViewHolder(holder: MyViewHolderMessagesDoc, position: Int) {
        val patient = patientList[position]
        holder.firstNamePat.text = patient.firstName
        holder.lastNamePat.text = patient.lastName
        holder.peselPat.text = patient.pesel
        holder.mailPat.text = patient.email
        holder.addressPat.text = patient.address
        holder.phonePat.text = patient.phone
    }

    override fun getItemCount(): Int {
        return patientList.size
    }
}
