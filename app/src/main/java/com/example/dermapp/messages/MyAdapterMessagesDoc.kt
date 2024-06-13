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
        val doctor = patientList[position]
        holder.firstNamePat.text = doctor.firstName
        holder.lastNamePat.text = doctor.lastName
        holder.peselPat.text = doctor.pesel
        holder.mailPat.text = doctor.email
        holder.addressPat.text = doctor.address
        holder.phonePat.text = doctor.phone
//        holder.imagePatMail.text = doctor.imageViewPatMessagesDoc3)
//        holder.imagePatLocalization.text = doctor.imageViewPatMessagesDoc4)
//        holder.imagePatPhone.text = doctor.imageViewPatMessagesDoc5)
    }

    override fun getItemCount(): Int {
        return patientList.size
    }
}
