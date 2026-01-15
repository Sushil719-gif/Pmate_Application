package com.example.pmate.Firestore.DataModels

data class StudentModel(
    val studentId: String = "",
    val name: String = "",
    val email: String = "",
    val branch: String = "",
    val rollNo: String = "",
    val appliedJobs: List<String> = emptyList()
)