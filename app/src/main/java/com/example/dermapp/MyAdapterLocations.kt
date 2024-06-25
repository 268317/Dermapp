package com.example.dermapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.database.Location

/**
 * Adapter class for displaying a list of locations in a RecyclerView.
 * @property locations List of locations to display.
 */
class LocationsAdapter(private val locations: List<String>) : RecyclerView.Adapter<LocationsAdapter.LocationViewHolder>() {

    /**
     * ViewHolder for each location item in the RecyclerView.
     * @param view The layout view for the location item.
     */
    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewLocation: TextView = view.findViewById(R.id.textViewLocation)
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new LocationViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_location_item, parent, false)
        return LocationViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.textViewLocation.text = location
    }

    /**
     * Returns the total number of locations in the data set held by the adapter.
     * @return The total number of locations in the data set.
     */
    override fun getItemCount(): Int = locations.size
}