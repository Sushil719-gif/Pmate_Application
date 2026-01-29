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

    val files: List<String> = emptyList(),
    val active: Boolean = true,


    var instructions: String = "",

    val status: String = "Active" ,  // Active | On Hold | Completed
    val batchYear: String = "",
    val eligibilityType: String = "UNPLACED_ONLY",

    val branches: List<String> = emptyList(),


    val timestamp: Long = System.currentTimeMillis()
)