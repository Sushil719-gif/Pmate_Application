package com.example.pmate.Firestore.DataModels

data class JobModel(
    val jobId: String = "",
    val company: String = "",
    val role: String = "",
    val stipend: String = "",
    val ctcLpa: Double = 0.0,

    val location: String = "",
    val jobType: String = "",
    val deadline: String = "",
    val deadlineTimestamp: Long = 0L,


    val description: String = "",

    val files: List<String> = emptyList(),
    val active: Boolean = true,


    var instructions: String = "",

    val status: String = "Active" ,  // Active | On Hold | Completed
    val batchYear: String = "",
    val eligibilityType: String = "UNPLACED_ONLY",

    val branches: List<String> = emptyList(),
    val minCgpa: Double = 0.0,
    val isDreamJob: Boolean = false,
    val dreamPackageLimit: Double = 0.0,
    val googleFormTemplateLink: String = "",
    var jobFormId: String = "",

    val createdAt: Long = System.currentTimeMillis(),


    val timestamp: Long = System.currentTimeMillis()
)