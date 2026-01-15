package com.example.pmate.Firestore.FirestoreRepository

import com.example.pmate.Firestore.DataModels.ApplicationModel

import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.DataModels.StudentModel
import com.example.pmate.ui.Admin.dashboard.company.NoticeModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    // ADD JOB (ADMIN)
    suspend fun addJob(job: JobModel): String? {
        return try {
            val docRef = db.collection("jobs").document()
            val jobWithId = job.copy(jobId = docRef.id)
            docRef.set(jobWithId).await()
            docRef.id   // ⭐ return jobId
        } catch (e: Exception) {
            null
        }
    }


    // FETCH ALL JOBS
    suspend fun getAllJobs(): List<JobModel> {
        return try {
            db.collection("jobs")
                .orderBy("timestamp")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(JobModel::class.java) }   // FIXED
        } catch (e: Exception) {
            emptyList()
        }
    }

    // FETCH JOB BY ID
    suspend fun getJobById(jobId: String): JobModel? {
        return try {
            db.collection("jobs")
                .document(jobId)
                .get()
                .await()
                .toObject(JobModel::class.java)   // FIXED
        } catch (e: Exception) {
            null
        }
    }

    // APPLY FOR JOB
    suspend fun applyForJob(jobId: String, studentId: String): Boolean {
        return try {
            val docRef = db.collection("applications").document()
            val application = ApplicationModel(
                applicationId = docRef.id,
                jobId = jobId,
                studentId = studentId
            )

            docRef.set(application).await()

            // update student job list
            db.collection("students")
                .document(studentId)
                .update("appliedJobs", FieldValue.arrayUnion(jobId))
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    // FETCH APPLICATIONS FOR ONE STUDENT
    suspend fun getStudentApplications(studentId: String): List<ApplicationModel> {
        return try {
            db.collection("applications")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(ApplicationModel::class.java) }   // FIXED
        } catch (e: Exception) {
            emptyList()
        }
    }

    // FETCH STUDENT DETAILS
    suspend fun getStudentById(studentId: String): StudentModel? {
        return try {
            db.collection("students")
                .document(studentId)
                .get()
                .await()
                .toObject(StudentModel::class.java)   // FIXED
        } catch (e: Exception) {
            null
        }
    }

    // UPDATE JOB
    suspend fun updateJob(job: JobModel): Boolean {
        return try {
            db.collection("jobs").document(job.jobId).set(job).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // DELETE JOB
    suspend fun deleteJob(jobId: String): Boolean {
        return try {
            db.collection("jobs").document(jobId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
 //To Update Job Status

    suspend fun updateJobStatus(jobId: String, newStatus: String) {
        db.collection("jobs")
            .document(jobId)
            .update("status", newStatus)
            .await()
    }

    //to get particular company important details

    suspend fun getJobByCompany(company: String): JobModel? {
        val snapshot = db.collection("jobs")
            .whereEqualTo("company", company)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject(JobModel::class.java)
    }

    //To send notices

    suspend fun addNotice(title: String, message: String) {
        val notice = hashMapOf(
            "title" to title,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("notices")
            .add(notice)
            .await()
    }

    suspend fun getAllNotices(): List<NoticeModel> {
        val snapshot = db.collection("notices").get().await()
        return snapshot.toObjects(NoticeModel::class.java)
    }



}
