package com.example.dermapp.database

interface LocationFirestoreInterface {
    suspend fun addLocation(location: Location)
    suspend fun updateLocation(locationId: String, location: Location)
    suspend fun deleteLocation(locationId: String)
    suspend fun getLocation(locationId: String): Location?
}


