package com.example.dermapp.database

import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

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

    override suspend fun addAppUser(appUser: AppUser) {
        try {
            db.collection("users").document(appUser.appUserId).set(appUser).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding app user", e)
        }
    }

    override suspend fun getAppUser(appUserId: String): AppUser? {
        val snapshot = db.collection("users")
            .whereEqualTo(FieldPath.documentId(), appUserId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<AppUser>()
    }

    override suspend fun updateAppUser(appUserId: String, updatedAppUser: AppUser) {
        try {
            db.collection("users").document(appUserId).set(updatedAppUser).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating app user", e)
        }
    }
    override suspend fun deleteAppUser(appUserId: String) {
        try {
            db.collection("users").document(appUserId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting app user", e)
        }
    }
    override suspend fun updateAppointment(appointmentId: String, updatedAppointment: Appointment) {
        try {
            db.collection("appointment").document(appointmentId)
                .set(updatedAppointment).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating appointment", e)
        }
    }

    override suspend fun getAppointment(appointmentId: String): Appointment? {
        val snapshot = db.collection("appointment")
            .whereEqualTo(FieldPath.documentId(), appointmentId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Appointment>()
    }

    override suspend fun deleteAppointment(appointmentId: String) {
        try {
            db.collection("appointment").document(appointmentId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting appointment", e)
        }
    }

    override suspend fun addAppointment(appointment: Appointment) {
        try {
            db.collection("appointment").document(appointment.appointmentId).set(appointment).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding appointment", e)
        }
    }

    override suspend fun addConversation(conversation: Conversation) {
        try {
            db.collection("conversation").document(conversation.conversationId).set(conversation).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding conversation", e)
        }
    }

    override suspend fun getConversation(conversationId: String): Conversation? {
        val snapshot = db.collection("conversation")
            .whereEqualTo(FieldPath.documentId(), conversationId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Conversation>()
    }

    override suspend fun updateConversation(conversationId: String, updatedConversation: Conversation) {
        try {
            db.collection("conversation").document(conversationId)
                .set(updatedConversation).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating conversation", e)
        }
    }

    override suspend fun deleteConversation(conversationId: String) {
        try {
            db.collection("conversation").document(conversationId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting conversation", e)
        }
    }

    override suspend fun addMessage(message: Message) {
        try {
            db.collection("message").document(message.messageId).set(message).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding message", e)
        }
    }

    override suspend fun getMessage(messageId: String): Message? {
        val snapshot = db.collection("message")
            .whereEqualTo(FieldPath.documentId(), messageId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Message>()
    }

    override suspend fun updateMessage(messageId: String, updatedMessage: Message) {
        try {
            db.collection("message").document(messageId).set(updatedMessage).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating message", e)
        }
    }

    override suspend fun deleteMessage(messageId: String) {
        try {
            db.collection("message").document(messageId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting message", e)
        }
    }

    override suspend fun addDoctor(doctor: Doctor) {
        try {
            db.collection("doctors").document(doctor.doctorId).set(doctor).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding doctor", e)
        }
    }

    override suspend fun getDoctor(doctorId: String): Doctor? {
        val snapshot = db.collection("doctors")
            .whereEqualTo(FieldPath.documentId(), doctorId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Doctor>()
    }

    override suspend fun updateDoctor(doctorId: String, updatedDoctor: Doctor) {
        try {
            db.collection("doctors").document(doctorId).set(updatedDoctor).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating doctor", e)
        }
    }

    override suspend fun deleteDoctor(doctorId: String) {
        try {
            db.collection("doctors").document(doctorId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting doctor", e)
        }
    }

    override suspend fun addPatient(patient: Patient) {
        try {
            db.collection("patients").document(patient.pesel).set(patient).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding patient", e)
        }
    }

    override suspend fun getPatient(pesel: String): Patient? {
        val snapshot = db.collection("patients")
            .whereEqualTo(FieldPath.documentId(), pesel)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Patient>()
    }

    override suspend fun updatePatient(pesel: String, updatedPatient: Patient) {
        try {
            db.collection("patients").document(pesel).set(updatedPatient).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating patient", e)
        }
    }

    override suspend fun deletePatient(pesel: String) {
        try {
            db.collection("patients").document(pesel).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting patient", e)
        }
    }

    override suspend fun addPrescription(prescription: Prescription) {
        try {
            db.collection("pescription").document(prescription.prescriptionId).set(prescription).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding pescription", e)
        }
    }

    override suspend fun getPrescription(prescriptionId: String): Prescription? {
        val snapshot = db.collection("pescription")
            .whereEqualTo(FieldPath.documentId(), prescriptionId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Prescription>()
    }

    override suspend fun updatePrescription(
        prescriptionId: String,
        updatedPrescription: Prescription) {
        try {
            db.collection("pescription").document(prescriptionId).set(updatedPrescription).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating pescription", e)
        }
    }

    override suspend fun deletePrescription(prescriptionId: String) {
        try {
            db.collection("pescription").document(prescriptionId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting pescription", e)
        }
    }

    override suspend fun addMedicalReport(medicalReport: MedicalReport) {
        try {
            db.collection("medicalReport").document(medicalReport.medicalReportId).set(medicalReport).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding medical report", e)
        }
    }

    override suspend fun getMedicalReport(medicalReportId: String): MedicalReport? {
        val snapshot = db.collection("medicalReport")
            .whereEqualTo(FieldPath.documentId(), medicalReportId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<MedicalReport>()
    }

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

    override suspend fun deleteMedicalReport(medicalReportId: String) {
        try {
            db.collection("medicalReport").document(medicalReportId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting medical report", e)
        }
    }

    override suspend fun addMedicalRecord(medicalRecord: MedicalRecord) {
        try {
            db.collection("medicalRecord").document(medicalRecord.medicalRecordId).set(medicalRecord).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding medical record", e)
        }
    }

    override suspend fun getMedicalRecord(medicalRecordId: String): MedicalRecord? {
        val snapshot = db.collection("medicalRecord")
            .whereEqualTo(FieldPath.documentId(), medicalRecordId)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<MedicalRecord>()
    }

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

    override suspend fun deleteMedicalRecord(medicalRecordId: String) {
        try {
            db.collection("medicalRecord").document(medicalRecordId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting medical record", e)
        }
    }
}
