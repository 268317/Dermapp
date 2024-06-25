package com.example.dermapp.database

/**
 * Interface defining Firestore operations for managing locations.
 */
interface LocationFirestoreInterface {
    /**
     * Adds a location to Firestore.
     *
     * @param location The location to be added.
     */
    suspend fun addLocation(location: Location)

    /**
     * Updates a location in Firestore.
     *
     * @param locationId The ID of the location to update.
     * @param location The updated location object.
     */
    suspend fun updateLocation(locationId: String, location: Location)

    /**
     * Deletes a location from Firestore.
     *
     * @param locationId The ID of the location to delete.
     */
    suspend fun deleteLocation(locationId: String)

    /**
     * Retrieves a location from Firestore.
     *
     * @param locationId The ID of the location to retrieve.
     * @return The retrieved location object, or null if not found.
     */
    suspend fun getLocation(locationId: String): Location?
}
