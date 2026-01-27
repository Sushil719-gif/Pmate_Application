package com.example.pmate.Firestore.DataModels

data class CompanyAnalytics(
    val company: String,
    val role: String,
    val location: String,
    val applicants: Int,
    val placed: Int,
    val selectionRate: Int,
    val status: String
)
