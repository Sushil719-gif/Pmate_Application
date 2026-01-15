package com.example.pmate.Firestore.DataModels

data class ApplicationModel(
    val applicationId: String = "",
    val jobId: String = "",
    val studentId: String = "",
    val appliedOn: Long = System.currentTimeMillis()
)