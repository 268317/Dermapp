package com.example.dermapp.database

interface AppointmentFirestoreInterface {
    suspend fun addAppointment(appointment: Appointment)
    suspend fun updateAppointment(appointmentId: String, updatedAppointment: Appointment)
    suspend fun deleteAppointment(appointmentId: String)
    suspend fun getAppointment(appointmentId: String): Appointment?

}