package com.example.pmate.SaasManagement



import com.google.firebase.firestore.FirebaseFirestore

object FirestorePaths {

    private fun collegeRoot(collegeId: String) =
        FirebaseFirestore.getInstance()
            .collection("colleges")
            .document(collegeId)

    fun students(collegeId: String) =
        collegeRoot(collegeId).collection("students")

    fun jobs(collegeId: String) =
        collegeRoot(collegeId).collection("jobs")

    fun applications(collegeId: String) =
        collegeRoot(collegeId).collection("applications")

    fun users(collegeId: String) =
        collegeRoot(collegeId).collection("users")

    fun notices(collegeId: String) =
        collegeRoot(collegeId).collection("notices")

    fun csvHistory(collegeId: String) =
        collegeRoot(collegeId).collection("csv_upload_history")
}
