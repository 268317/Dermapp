package com.example.dermapp.database

import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

/**
 * Class responsible for handling Firestore database operations for various entities in the dermatology application.
 *
 * @property db Instance of FirebaseFirestore used to interact with Firestore database.
 */
class FirestoreDatabaseOperations(private val db: FirebaseFirestore) :
    AppointmentFirestoreInterface,
    AppUserFirestoreInterface,
    DoctorFirestoreInterface,
    MedicalRecordFirestoreInterface,
    MedicalReportFirestoreInterface,
    PatientFirestoreInterface,
    PrescriptionFirestoreInterface,
    ConversationFirestoreInterface,
    MessageFirestoreInterface {

    companion object {
        private const val TAG = "FirestoreDatabaseOps"
    }

    /**
     * Adds an application user to the Firestore database.
     *
     * @param appUser The application user to be added.
     */
    override suspend fun addAppUser(appUser: AppUser) {
        try {
            db.collection("users").document(appUser.appUserId).set(appUser).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding app user", e)
        }
    }

    /**
     * Retrieves an application user from the Firestore database.
     *
     * @param appUserId The ID of the application user to retrieve.
     * @return The retrieved application user, or null if not found.
     */
    override suspend fun getAppUser(appUserId: String): AppUser? {
        val snapshot = db.collection("users")
            .whereEqualTo(FieldPath.documentId(), appUserId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<AppUser>()
    }

    /**
     * Updates an application user in the Firestore database.
     *
     * @param appUserId The ID of the application user to update.
     * @param updatedAppUser The updated application user data.
     */
    override suspend fun updateAppUser(appUserId: String, updatedAppUser: AppUser) {
        try {
            db.collection("users").document(appUserId).set(updatedAppUser).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating app user", e)
        }
    }

    /**
     * Deletes an application user from the Firestore database.
     *
     * @param appUserId The ID of the application user to delete.
     */
    override suspend fun deleteAppUser(appUserId: String) {
        try {
            db.collection("users").document(appUserId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting app user", e)
        }
    }

    /**
     * Updates an appointment in the Firestore database.
     *
     * @param appointmentId The ID of the appointment to update.
     * @param updatedAppointment The updated appointment data.
     */
    override suspend fun updateAppointment(appointmentId: String, updatedAppointment: Appointment) {
        try {
            db.collection("appointment").document(appointmentId)
                .set(updatedAppointment).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating appointment", e)
        }
    }

    /**
     * Retrieves an appointment from the Firestore database.
     *
     * @param appointmentId The ID of the appointment to retrieve.
     * @return The retrieved appointment, or null if not found.
     */
    override suspend fun getAppointment(appointmentId: String): Appointment? {
        val snapshot = db.collection("appointment")
            .whereEqualTo(FieldPath.documentId(), appointmentId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Appointment>()
    }

    /**
     * Deletes an appointment from the Firestore database.
     *
     * @param appointmentId The ID of the appointment to delete.
     */
    override suspend fun deleteAppointment(appointmentId: String) {
        try {
            db.collection("appointment").document(appointmentId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting appointment", e)
        }
    }

    /**
     * Adds an appointment to the Firestore database.
     *
     * @param appointment The appointment to be added.
     */
    override suspend fun addAppointment(appointment: Appointment) {
        try {
            db.collection("appointment").document(appointment.appointmentId).set(appointment).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding appointment", e)
        }
    }

    /**
     * Adds a conversation to the Firestore database.
     *
     * @param conversation The conversation to be added.
     */
    override suspend fun addConversation(conversation: Conversation) {
        try {
            db.collection("conversation").document(conversation.conversationId).set(conversation).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding conversation", e)
        }
    }

    /**
     * Retrieves a conversation from the Firestore database.
     *
     * @param conversationId The ID of the conversation to retrieve.
     * @return The retrieved conversation, or null if not found.
     */
    override suspend fun getConversation(conversationId: String): Conversation? {
        val snapshot = db.collection("conversation")
            .whereEqualTo(FieldPath.documentId(), conversationId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Conversation>()
    }

    /**
     * Updates a conversation in the Firestore database.
     *
     * @param conversationId The ID of the conversation to update.
     * @param updatedConversation The updated conversation data.
     */
    override suspend fun updateConversation(conversationId: String, updatedConversation: Conversation) {
        try {
            db.collection("conversation").document(conversationId)
                .set(updatedConversation).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating conversation", e)
        }
    }

    /**
     * Deletes a conversation from the Firestore database.
     *
     * @param conversationId The ID of the conversation to delete.
     */
    override suspend fun deleteConversation(conversationId: String) {
        try {
            db.collection("conversation").document(conversationId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting conversation", e)
        }
    }

    /**
     * Adds a message to the Firestore database.
     *
     * @param message The message to be added.
     */
    override suspend fun addMessage(message: Message) {
        try {
            db.collection("message").document(message.messageId).set(message).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding message", e)
        }
    }

    /**
     * Retrieves a message from the Firestore database.
     *
     * @param messageId The ID of the message to retrieve.
     * @return The retrieved message, or null if not found.
     */
    override suspend fun getMessage(messageId: String): Message? {
        val snapshot = db.collection("message")
            .whereEqualTo(FieldPath.documentId(), messageId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Message>()
    }

    /**
     * Updates a message in the Firestore database.
     *
     * @param messageId The ID of the message to update.
     * @param updatedMessage The updated message data.
     */
    override suspend fun updateMessage(messageId: String, updatedMessage: Message) {
        try {
            db.collection("message").document(messageId).set(updatedMessage).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating message", e)
        }
    }

    /**
     * Deletes a message from the Firestore database.
     *
     * @param messageId The ID of the message to delete.
     */
    override suspend fun deleteMessage(messageId: String) {
        try {
            db.collection("message").document(messageId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting message", e)
        }
    }

    /**
     * Adds a doctor to the Firestore database.
     *
     * @param doctor The doctor to be added.
     */
    override suspend fun addDoctor(doctor: Doctor) {
        try {
            db.collection("doctors").document(doctor.doctorId).set(doctor).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding doctor", e)
        }
    }

    /**
     * Retrieves a doctor from the Firestore database.
     *
     * @param doctorId The ID of the doctor to retrieve.
     * @return The retrieved doctor, or null if not found.
     */
    override suspend fun getDoctor(doctorId: String): Doctor? {
        val snapshot = db.collection("doctors")
            .whereEqualTo(FieldPath.documentId(), doctorId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Doctor>()
    }

    /**
     * Updates a doctor in the Firestore database.
     *
     * @param doctorId The ID of the doctor to update.
     * @param updatedDoctor The updated doctor data.
     */
    override suspend fun updateDoctor(doctorId: String, updatedDoctor: Doctor) {
        try {
            db.collection("doctors").document(doctorId).set(updatedDoctor).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating doctor", e)
        }
    }

    /**
     * Deletes a doctor from the Firestore database.
     *
     * @param doctorId The ID of the doctor to delete.
     */
    override suspend fun deleteDoctor(doctorId: String) {
        try {
            db.collection("doctors").document(doctorId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting doctor", e)
        }
    }

    /**
     * Adds a patient to the Firestore database.
     *
     * @param patient The patient to be added.
     */
    override suspend fun addPatient(patient: Patient) {
        try {
            db.collection("patients").document(patient.pesel).set(patient).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding patient", e)
        }
    }

    /**
     * Retrieves a patient from the Firestore database.
     *
     * @param pesel The PESEL (Personal Identification Number) of the patient to retrieve.
     * @return The retrieved patient, or null if not found.
     */
    override suspend fun getPatient(pesel: String): Patient? {
        val snapshot = db.collection("patients")
            .whereEqualTo(FieldPath.documentId(), pesel)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Patient>()
    }

    /**
     * Updates a patient in the Firestore database.
     *
     * @param pesel The PESEL of the patient to update.
     * @param updatedPatient The updated patient data.
     */
    override suspend fun updatePatient(pesel: String, updatedPatient: Patient) {
        try {
            db.collection("patients").document(pesel).set(updatedPatient).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating patient", e)
        }
    }

    /**
     * Deletes a patient from the Firestore database.
     *
     * @param pesel The PESEL of the patient to delete.
     */
    override suspend fun deletePatient(pesel: String) {
        try {
            db.collection("patients").document(pesel).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting patient", e)
        }
    }

    /**
     * Adds a prescription to the Firestore database.
     *
     * @param prescription The prescription to be added.
     */
    override suspend fun addPrescription(prescription: Prescription) {
        try {
            db.collection("pescription").document(prescription.prescriptionId).set(prescription).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding pescription", e)
        }
    }

    /**
     * Retrieves a prescription from the Firestore database.
     *
     * @param prescriptionId The ID of the prescription to retrieve.
     * @return The retrieved prescription, or null if not found.
     */
    override suspend fun getPrescription(prescriptionId: String): Prescription? {
        val snapshot = db.collection("pescription")
            .whereEqualTo(FieldPath.documentId(), prescriptionId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Prescription>()
    }

    /**
     * Updates a prescription in the Firestore database.
     *
     * @param prescriptionId The ID of the prescription to update.
     * @param updatedPrescription The updated prescription data.
     */
    override suspend fun updatePrescription(
        prescriptionId: String,
        updatedPrescription: Prescription) {
        try {
            db.collection("pescription").document(prescriptionId).set(updatedPrescription).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating pescription", e)
        }
    }

    /**
     * Deletes a prescription from the Firestore database.
     *
     * @param prescriptionId The ID of the prescription to delete.
     */
    override suspend fun deletePrescription(prescriptionId: String) {
        try {
            db.collection("pescription").document(prescriptionId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting pescription", e)
        }
    }

    /**
     * Adds a medical report to the Firestore database.
     *
     * @param medicalReport The medical report to be added.
     */
    override suspend fun addMedicalReport(medicalReport: MedicalReport) {
        try {
            db.collection("medicalReport").document(medicalReport.medicalReportId).set(medicalReport).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding medical report", e)
        }
    }

    /**
     * Retrieves a medical report from the Firestore database.
     *
     * @param medicalReportId The ID of the medical report to retrieve.
     * @return The retrieved medical report, or null if not found.
     */
    override suspend fun getMedicalReport(medicalReportId: String): MedicalReport? {
        val snapshot = db.collection("medicalReport")
            .whereEqualTo(FieldPath.documentId(), medicalReportId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<MedicalReport>()
    }

    /**
     * Updates a medical report in the Firestore database.
     *
     * @param medicalReportId The ID of the medical report to update.
     * @param updatedMedicalReport The updated medical report data.
     */
    override suspend fun updateMedicalReport(
        medicalReportId: String,
        updatedMedicalReport: MedicalRecord
    ) {
        try {
            db.collection("medicalReport").document(medicalReportId).set(updatedMedicalReport).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating medical report", e)
        }
    }

    /**
     * Deletes a medical report from the Firestore database.
     *
     * @param medicalReportId The ID of the medical report to delete.
     */
    override suspend fun deleteMedicalReport(medicalReportId: String) {
        try {
            db.collection("medicalReport").document(medicalReportId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting medical report", e)
        }
    }

    /**
     * Adds a medical record to the Firestore database.
     *
     * @param medicalRecord The medical record to be added.
     */
    override suspend fun addMedicalRecord(medicalRecord: MedicalRecord) {
        try {
            db.collection("medicalRecord").document(medicalRecord.medicalRecordId).set(medicalRecord).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding medical record", e)
        }
    }

    /**
     * Retrieves a medical record from the Firestore database.
     *
     * @param medicalRecordId The ID of the medical record to retrieve.
     * @return The retrieved medical record, or null if not found.
     */
    override suspend fun getMedicalRecord(medicalRecordId: String): MedicalRecord? {
        val snapshot = db.collection("medicalRecord")
            .whereEqualTo(FieldPath.documentId(), medicalRecordId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<MedicalRecord>()
    }

    /**
     * Updates a medical record in the Firestore database.
     *
     * @param medicalRecordId The ID of the medical record to update.
     * @param updatedMedicalRecord The updated medical record data.
     */
    override suspend fun updateMedicalRecord(
        medicalRecordId: String,
        updatedMedicalRecord: MedicalRecord
    ) {
        try {
            db.collection("medicalRecord").document(medicalRecordId).set(updatedMedicalRecord).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating medical record", e)
        }
    }

    /**
     * Deletes a medical record from the Firestore database.
     *
     * @param medicalRecordId The ID of the medical record to delete.
     */
    override suspend fun deleteMedicalRecord(medicalRecordId: String) {
        try {
            db.collection("medicalRecord").document(medicalRecordId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting medical record", e)
        }
    }
}

