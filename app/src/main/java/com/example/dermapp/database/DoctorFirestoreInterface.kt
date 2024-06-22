package com.example.dermapp.database

import com.example.dermapp.database.Doctor

interface DoctorFirestoreInterface {
    suspend fun addDoctor(doctor: Doctor)
    suspend fun updateDoctor(doctorId: String, updatedDoctor: Doctor)
    suspend fun deleteDoctor(doctorId: String)
    suspend fun getDoctor(doctorId: String): Doctor?
}