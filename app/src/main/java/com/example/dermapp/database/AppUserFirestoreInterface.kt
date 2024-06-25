package com.example.dermapp.database

/**
 * Interface for handling Firestore operations related to app users.
 */
interface AppUserFirestoreInterface {
    /**
     * Adds a new user to Firestore.
     *
     * @param appUser The user to be added.
     */
    suspend fun addAppUser(appUser: AppUser)

    /**
     * Updates an existing user in Firestore.
     *
     * @param appUserId The unique identifier of the user to be updated.
     * @param updatedAppUser The updated user data.
     */
    suspend fun updateAppUser(appUserId: String, updatedAppUser: AppUser)

    /**
     * Deletes a user from Firestore.
     *
     * @param appUserId The unique identifier of the user to be deleted.
     */
    suspend fun deleteAppUser(appUserId: String)

    /**
     * Retrieves a user from Firestore.
     *
     * @param appUserId The unique identifier of the user to be retrieved.
     * @return The retrieved user, or null if no user is found.
     */
    suspend fun getAppUser(appUserId: String): AppUser?
}
