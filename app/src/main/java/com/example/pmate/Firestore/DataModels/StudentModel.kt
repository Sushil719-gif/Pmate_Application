package com.example.pmate.Firestore.DataModels

data class StudentModel(
    val name: String = "",
    val email: String = "",
    val usn: String = "",
    val branch: String = "",
    val batchYear: String = "",
    val status: String = "ACTIVE",
    val placementStatus: String = "IN_PROCESS",
    val cgpa: Double = 0.0,
    val backlogs: Int = 0,
    val offers: List<Double> = emptyList(),
    val gender: String = "",
    val phone: String = ""


)

