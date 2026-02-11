package com.example.pmate.Firestore.DataModels

data class Applicant(
    val jobId: String = "",
    val studentId: String = "",
    val status: String = "UNDER_REVIEW",
    val appliedAt: Long = 0L,
    val oaDetails: OADetails? = null

)


