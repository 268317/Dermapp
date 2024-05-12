package com.example.dermapp.database

import com.google.firebase.firestore.PropertyName
import com.google.type.DateTime
import java.util.Date

open class MedicalReport(
    @get:PropertyName("medicalReportId") @set:PropertyName("medicalReportId") open var medicalReportId: String = "",
    @get:PropertyName("doctorId") @set:PropertyName("doctorId") open var doctorId: String = "",
    @get:PropertyName("patientPesel") @set:PropertyName("patientPesel") open var patientPesel: String = "",
    @get:PropertyName("date") @set:PropertyName("date") open var reportDate: DateTime,
    @get:PropertyName("itching") @set:PropertyName("itching") open var itching: Boolean,
    @get:PropertyName("rash") @set:PropertyName("rash") open var rash: Boolean,
    @get:PropertyName("redness") @set:PropertyName("redness") open var redness: Boolean,
    @get:PropertyName("newMole") @set:PropertyName("newMole") open var newMole: Boolean,
    @get:PropertyName("moleChanges") @set:PropertyName("moleChanges") open var moleChanges: Boolean,
    @get:PropertyName("blackheads") @set:PropertyName("blackheads") open var blackheads: Boolean,
    @get:PropertyName("pimples") @set:PropertyName("pimples") open var pimples: Boolean,
    @get:PropertyName("warts") @set:PropertyName("warts") open var warts: Boolean,
    @get:PropertyName("dryness") @set:PropertyName("dryness") open var dryness: Boolean,
    @get:PropertyName("severeAcne") @set:PropertyName("severeAcne") open var severeAcne: Boolean,
    @get:PropertyName("seborrhoea") @set:PropertyName("seborrhoea") open var seborrhoea: Boolean,
    @get:PropertyName("discoloration") @set:PropertyName("discoloration") open var discoloration: Boolean,
    @get:PropertyName("otherInfo") @set:PropertyName("otherInfo") open var otherInfo: String = ""
)