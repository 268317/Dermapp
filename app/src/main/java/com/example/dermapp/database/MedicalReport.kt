package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName

data class MedicalReport(
    @get:PropertyName("medicalReportId") @set:PropertyName("medicalReportId") open var medicalReportId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") open var doctorId: String = "",
    @get:PropertyName("patientPesel") @set:PropertyName("patientPesel") open var patientPesel: String = "",
    @get:PropertyName("date") @set:PropertyName("date") open var reportDate: String,
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
)
