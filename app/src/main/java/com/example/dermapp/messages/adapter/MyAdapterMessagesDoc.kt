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
import com.example.dermapp.messages.NewMessagePatActivity
import com.example.dermapp.messages.holder.MyViewHolderMessagesDoc

/**
 * Adapter for populating a RecyclerView with a list of patients for doctor messages.
 *
 * @param patientsList The list of Patient objects to be displayed.
 */
class MyAdapterMessagesDoc (private val context: Context, private var patientsList: List<Patient>) : RecyclerView.Adapter<MyViewHolderMessagesDoc>()  {

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new MyViewHolderMessagesDoc that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderMessagesDoc {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_doc_view_activity, parent, false)
        return MyViewHolderMessagesDoc(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: MyViewHolderMessagesDoc, position: Int) {
        val patient = patientsList[position]
        holder.firstNamePat.text = patient.firstName
        holder.lastNamePat.text = patient.lastName

        // Use Glide to load the profile photo URL into imageDoc
        Glide.with(context)
            .load(patient.profilePhoto) // Assuming doctor.profilePhoto contains the URL
            .apply(RequestOptions.bitmapTransform(CircleCrop())) // Make image circular
            .placeholder(R.drawable.black_account_circle) // Optional: Add a placeholder
            .error(R.drawable.black_account_circle) // Optional: Add an error image if URL fails
            .into(holder.imagePat)

        // Set status indicator drawable based on the `isOnline` field
        val statusDrawable = if (patient.isOnline) R.drawable.status_indicator_background_online else R.drawable.status_indicator_background_offline
        holder.statusIndicatorDoc.setBackgroundResource(statusDrawable)

        // Set OnClickListener for the imageDoc
        holder.imagePat.setOnClickListener {
            val intent = Intent(context, NewMessagePatActivity::class.java)
            intent.putExtra("patientId", patient.appUserId)
            context.startActivity(intent)
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in the data set.
     */
    override fun getItemCount(): Int {
        return patientsList.size
    }

    /**
     * Update the list of patients displayed by the adapter.
     *
     * @param patients The new list of Doctor objects to be displayed.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setPatientsList(patients: List<Patient>) {
        this.patientsList = patients
        notifyDataSetChanged()
    }
}
