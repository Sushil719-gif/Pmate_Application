package com.example.pmate.Firestore.DataModels

data class StudentModel(
    val name: String = "",
    val email: String = "",
    val usn: String = "",
    val branch: String = "",
    val batchYear: String = "",
    val status: String = "ACTIVE",
    val placementStatus: String = "IN_PROCESS"
)

