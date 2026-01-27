package com.example.pmate.Firestore.DataModels



data class UserModel(
    val name: String = "",
    val usn: String = "",
    val email: String = "",
    val role: String = "",
    val branch: String = "",
    val batchYear: String = ""
)

