package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

/**
 * Data class representing a medical report in the Firestore database.
 *
 * @property medicalReportId The ID of the medical report.
 * @property doctorId The ID of the doctor associated with the medical report.
 * @property patientPesel The PESEL (Personal Identification Number) of the patient associated with the medical report.
 * @property date The date of the medical report.
 * @property itching Indicates if itching is present in the medical report.
 * @property rash Indicates if rash is present in the medical report.
 * @property redness Indicates if redness is present in the medical report.
 * @property newMole Indicates if a new mole is reported in the medical report.
 * @property moleChanges Indicates if there are changes in moles reported in the medical report.
 * @property blackheads Indicates if blackheads are reported in the medical report.
 * @property pimples Indicates if pimples are reported in the medical report.
 * @property warts Indicates if warts are reported in the medical report.
 * @property dryness Indicates if dryness is reported in the medical report.
 * @property severeAcne Indicates if severe acne is reported in the medical report.
 * @property seborrhoea Indicates if seborrhoea is reported in the medical report.
 * @property discoloration Indicates if discoloration is reported in the medical report.
 * @property otherInfo Additional information provided in the medical report.
 * @property attachmentUrl The URL of any attachment (e.g., image) associated with the medical report.
 */
data class MedicalReport(
    @get:PropertyName("medicalReportId") @set:PropertyName("medicalReportId") open var medicalReportId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") open var doctorId: String = "",
    @get:PropertyName("patientPesel") @set:PropertyName("patientPesel") open var patientPesel: String = "",
    @get:PropertyName("date") @set:PropertyName("date") open var date: String,
    @get:PropertyName("itching") @set:PropertyName("itching") open var itching: Boolean = false,
    @get:PropertyName("rash") @set:PropertyName("rash") open var rash: Boolean = false,
    @get:PropertyName("redness") @set:PropertyName("redness") open var redness: Boolean = false,
    @get:PropertyName("newMole") @set:PropertyName("newMole") open var newMole: Boolean = false,
    @get:PropertyName("moleChanges") @set:PropertyName("moleChanges") open var moleChanges: Boolean = false,
    @get:PropertyName("blackheads") @set:PropertyName("blackheads") open var blackheads: Boolean = false,
    @get:PropertyName("pimples") @set:PropertyName("pimples") open var pimples: Boolean = false,
    @get:PropertyName("warts") @set:PropertyName("warts") open var warts: Boolean = false,
    @get:PropertyName("dryness") @set:PropertyName("dryness") open var dryness: Boolean = false,
    @get:PropertyName("severeAcne") @set:PropertyName("severeAcne") open var severeAcne: Boolean = false,
    @get:PropertyName("seborrhoea") @set:PropertyName("seborrhoea") open var seborrhoea: Boolean = false,
    @get:PropertyName("discoloration") @set:PropertyName("discoloration") open var discoloration: Boolean = false,
    @get:PropertyName("otherInfo") @set:PropertyName("otherInfo") open var otherInfo: String = "",
    @get:PropertyName("attachmentUrl") @set:PropertyName("attachmentUrl") open var attachmentUrl: String = ""  // Nowe pole
){
    constructor() : this("", "", "", "", false, false, false, false, false, false,
        false, false, false, false, false, false, "")
}
