package com.example.dermapp.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.R
import com.example.dermapp.database.Patient

/**
 * Adapter for populating a RecyclerView with a list of patients for doctor messages.
 *
 * @param patientList The list of Patient objects to be displayed.
 */
class MyAdapterMessagesDoc (private val patientList: List<Patient>) : RecyclerView.Adapter<MyViewHolderMessagesDoc>()  {

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new MyViewHolderMessagesDoc that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderMessagesDoc {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.messages_doc_view_activity, parent, false)
        return MyViewHolderMessagesDoc(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
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

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in the data set.
     */
    override fun getItemCount(): Int {
        return patientList.size
    }
}
