package com.example.dermapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermapp.database.Location

class LocationsAdapter(private val locations: List<String>) : RecyclerView.Adapter<LocationsAdapter.LocationViewHolder>() {

    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewLocation: TextView = view.findViewById(R.id.textViewLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_location_item, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.textViewLocation.text = location
    }

    override fun getItemCount(): Int = locations.size
}