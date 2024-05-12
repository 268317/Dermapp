package com.example.dermapp.database

interface AppointmentFirestoreInterface {
    fun addAppointment(appointment: Appointment)
    fun updateAppointment(appointmentId: String, updatedAppointment: Map<String, Any>)
    fun deleteAppointment(appointmentId: String)
    fun getAppointment(appointmentId: String)

}