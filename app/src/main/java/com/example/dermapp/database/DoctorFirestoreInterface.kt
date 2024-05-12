package com.example.dermapp.database

interface DoctorFirestoreInterface {
    fun addDoctor(doctor: Doctor)
    fun updateDoctor(doctorId: String, updatedDoctor: Map<String, Any>)
    fun deleteDoctor(doctorId: String)
    fun getDoctor(doctorId: String)
}