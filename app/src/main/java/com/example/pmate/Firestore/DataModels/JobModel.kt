package com.example.pmate.Firestore.DataModels

data class JobModel(
    val jobId: String = "",
    val company: String = "",
    val role: String = "",
    val stipend: String = "",
    val location: String = "",
    val jobType: String = "",
    val deadline: String = "",
    val description: String = "",
    val skills: List<String> = emptyList(),
    val files: List<String> = emptyList(),

    var instructions: String = "",

    val status: String = "Active" ,  // Active | On Hold | Completed

    val timestamp: Long = System.currentTimeMillis()
)