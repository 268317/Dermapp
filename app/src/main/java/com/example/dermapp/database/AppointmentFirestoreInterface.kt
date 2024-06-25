package com.example.dermapp.database

/**
 * Interface for handling Firestore operations related to appointments.
 */
interface AppointmentFirestoreInterface {
    /**
     * Adds a new appointment to Firestore.
     *
     * @param appointment The appointment to be added.
     */
    suspend fun addAppointment(appointment: Appointment)

    /**
     * Updates an existing appointment in Firestore.
     *
     * @param appointmentId The unique identifier of the appointment to be updated.
     * @param updatedAppointment The updated appointment data.
     */
    suspend fun updateAppointment(appointmentId: String, updatedAppointment: Appointment)

    /**
     * Deletes an appointment from Firestore.
     *
     * @param appointmentId The unique identifier of the appointment to be deleted.
     */
    suspend fun deleteAppointment(appointmentId: String)

    /**
     * Retrieves an appointment from Firestore.
     *
     * @param appointmentId The unique identifier of the appointment to be retrieved.
     * @return The retrieved appointment, or null if no appointment is found.
     */
    suspend fun getAppointment(appointmentId: String): Appointment?
}
