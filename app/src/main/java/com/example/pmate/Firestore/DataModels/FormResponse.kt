package com.example.pmate.Firestore.DataModels

data class FormResponse(
    val responseId: String = "",
    val jobFormId: String = "",
    val jobId: String = "",
    val studentId: String = "",
    val answers: Map<String, String> = emptyMap()
)

