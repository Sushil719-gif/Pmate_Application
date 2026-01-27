package com.example.pmate.Firestore.FirestoreRepository

import android.util.Log
import com.example.pmate.Firestore.DataModels.Applicant
import com.example.pmate.Firestore.DataModels.ApplicationModel
import com.example.pmate.Firestore.DataModels.CompanyAnalytics
import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.DataModels.NoticeModel
import com.example.pmate.Firestore.DataModels.StudentModel
import com.example.pmate.Firestore.DataModels.UserModel
import com.example.pmate.ThreeBatchesAccess.BatchUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ADD JOB (ADMIN)
    suspend fun addJob(job: JobModel): String? {
        return try {
            val docRef = db.collection("jobs").document()
            val jobWithId = job.copy(jobId = docRef.id)
            docRef.set(jobWithId).await()
            docRef.id   //  return jobId
        } catch (e: Exception) {
            null
        }
    }


    // FETCH ALL JOBS
//    suspend fun getAllJobs(): List<JobModel> {
//
//        val snapshot = db.collection("jobs")
//            .whereEqualTo("isActive", true)
//            .orderBy("timestamp", Query.Direction.DESCENDING)
//            .get()
//            .await()
//
//        return snapshot.toObjects(JobModel::class.java)
//    }

    suspend fun getAllJobs(): List<JobModel> {

        val snapshot = db.collection("jobs")
            .whereNotEqualTo("status", "Archived")   // only hide archived
            .get()
            .await()

        return snapshot.toObjects(JobModel::class.java)
            .sortedByDescending { it.timestamp }
    }





    // FETCH JOB BY ID
    suspend fun getJobById(jobId: String): JobModel? {
        return try {
            db.collection("jobs")
                .document(jobId)
                .get()
                .await()
                .toObject(JobModel::class.java)
                ?.copy(jobId = jobId)   //  THIS LINE FIXES EVERYTHING
        } catch (e: Exception) {
            null
        }
    }


    // APPLY FOR JOB
    suspend fun applyForJob(jobId: String, studentId: String): Boolean {
        return try {
            val alreadyApplied = db.collection("applications")
                .whereEqualTo("jobId", jobId)
                .whereEqualTo("studentId", studentId)
                .get()
                .await()

            if (!alreadyApplied.isEmpty) return false

            db.collection("applications").add(
                mapOf(
                    "jobId" to jobId,
                    "studentId" to studentId,
                    "appliedAt" to System.currentTimeMillis()
                )
            ).await()

            true
        } catch (e: Exception) {
            false
        }
    }


    fun isDeadlinePassed(deadline: String): Boolean {
        return try {
            val parts = deadline.split("/")
            val day = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val year = parts[2].toInt()

            val deadlineCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.MONTH, month)
                set(Calendar.YEAR, year)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
            }

            Calendar.getInstance().after(deadlineCal)
        } catch (e: Exception) {
            true // safest: block apply if parsing fails
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
            val doc = db
                .collection("students")
                .document(studentId)
                .get()
                .await()

            doc.toObject(StudentModel::class.java)
        } catch (e: Exception) {
            null
        }
    }




    //Fetch user details

    suspend fun getUserById(uid: String): UserModel? {
        return try {
            val doc = db.collection("users")
                .document(uid)
                .get()
                .await()

            doc.toObject(UserModel::class.java)
        } catch (e: Exception) {
            null
        }
    }


    //To prevent duplicate applications
    suspend fun hasAlreadyApplied(
        jobId: String,
        studentId: String
    ): Boolean {
        val snapshot = db
            .collection("applications")
            .whereEqualTo("jobId", jobId)
            .whereEqualTo("studentId", studentId)
            .get()
            .await()

        return !snapshot.isEmpty
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

    //To send notices..............................


    suspend fun addNotice(
        title: String,
        message: String,
        batch: String,
        validDays: Int,
        level: String
    ) {

        val now = System.currentTimeMillis()

        val notice = NoticeModel(
            title = title,
            message = message,
            batch = batch,
            level = level,
            timestamp = now,
            validTill = now + (validDays * 24 * 60 * 60 * 1000L)
        )

        db.collection("notices")
            .add(notice)
            .await()
    }




    suspend fun getAllNotices(): List<NoticeModel> {

        val now = System.currentTimeMillis()

        return db.collection("notices")
            .get()
            .await()
            .toObjects(NoticeModel::class.java)
            .filter { it.validTill > now }   // hide expired notices
    }




    suspend fun getUserByEmailAndPassword(email: String, password: String): UserModel? {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(UserModel::class.java)
            } else null

        } catch (e: Exception) {
            null
        }
    }

    suspend fun loginUser(email: String, password: String): UserModel? {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(UserModel::class.java)
            } else null

        } catch (e: Exception) {
            null
        }
    }

    suspend fun getApplicantsForJob(
        jobId: String
    ): List<Pair<String, Pair<Applicant, StudentModel>>> {

        val apps = db.collection("applications")
            .whereEqualTo("jobId", jobId)
            .get()
            .await()

        val result = mutableListOf<Pair<String, Pair<Applicant, StudentModel>>>()

        for (doc in apps.documents) {

            val applicant = doc.toObject(Applicant::class.java) ?: continue
            val studentDoc = db.collection("students")
                .document(applicant.studentId)
                .get()
                .await()

            val student = studentDoc.toObject(StudentModel::class.java) ?: continue

            result.add(
                doc.id to (applicant to student)   //  applicationId included
            )
        }

        return result
    }



// update student's placement status

    suspend fun updateApplicationStatus(
        applicationId: String,
        studentId: String,
        status: String
    ) {
        // Update application status
        db.collection("applications")
            .document(applicationId)
            .update("status", status)
            .await()

        // 🔥 Recalculate student's placement status properly
        val apps = db.collection("applications")
            .whereEqualTo("studentId", studentId)
            .get()
            .await()

        val isPlacedAnywhere = apps.documents.any {
            it.getString("status") == "PLACED"
        }

        db.collection("students")
            .document(studentId)
            .update(
                "placementStatus",
                if (isPlacedAnywhere) "PLACED" else "UNPLACED"
            )
            .await()
    }




    //student collection


    // CREATE STUDENT PROFILE IF NOT EXISTS
    suspend fun createStudentIfNotExists(
        uid: String,
        email: String
    ) {
        val docRef = db.collection("students").document(uid)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            docRef.set(
                mapOf(
                    "name" to email.substringBefore("@"),
                    "email" to email,
                    "batchYear" to "",          // can be updated later
                    "status" to "ACTIVE",
                    "placementStatus" to "UNPLACED",
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()
        }
    }
    // AUTO-SYNC STUDENT PROFILE FROM USERS (ONE TIME)
    suspend fun createStudentFromUserIfNotExists(
        uid: String,
        user: UserModel
    ) {
        val docRef = db.collection("students").document(uid)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            docRef.set(
                mapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "usn" to user.usn,
                    "branch" to user.branch,
                    "batchYear" to user.batchYear,
                    "status" to "ACTIVE",
                    "placementStatus" to "IN_PROCESS",
                    "createdAt" to System.currentTimeMillis()
                )
            )
                .await()
        }
    }


//Student applications

//    suspend fun getStudentApplications(): List<Applicant> {
//
//        val currentUserId = auth.currentUser?.uid ?: return emptyList()
//
//        val applicationsSnapshot = db
//            .collection("applications")
//            .whereEqualTo("uid", currentUserId)
//            .get()
//            .await()
//
//        return applicationsSnapshot.documents.mapNotNull { doc ->
//
//            val applicant = doc.toObject(Applicant::class.java)
//            applicant?.copy(
//                uid = doc.getString("uid") ?: currentUserId
//            )
//        }
//    }


    suspend fun getStudentApplications(): List<Applicant> {

        val currentUserId = auth.currentUser?.uid ?: return emptyList()

        val snapshot = db
            .collection("applications")
            .whereEqualTo("studentId", currentUserId) // ✅ CORRECT
            .get()
            .await()

        Log.d("STUDENT_APPS", "docs count = ${snapshot.size()}")

        return snapshot.documents.mapNotNull {
            it.toObject(Applicant::class.java)
        }
    }

// realtime listener for student applications
fun listenStudentApplications(
    onResult: (List<Applicant>) -> Unit
) {
    val currentUserId = auth.currentUser?.uid ?: return

    db.collection("applications")
        .whereEqualTo("studentId", currentUserId)
        .addSnapshotListener { snapshot, _ ->

            if (snapshot == null) return@addSnapshotListener

            val apps = snapshot.documents.mapNotNull {
                it.toObject(Applicant::class.java)
            }

            onResult(apps)
        }
}


    fun listenApplicantsForJob(
        jobId: String,
        onResult: (List<Pair<String, Pair<Applicant, StudentModel>>>) -> Unit
    ) {
        db.collection("applications")
            .whereEqualTo("jobId", jobId)
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) return@addSnapshotListener

                if (snapshot.isEmpty) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val result = mutableListOf<Pair<String, Pair<Applicant, StudentModel>>>()

                snapshot.documents.forEach { doc ->

                    val applicant = doc.toObject(Applicant::class.java) ?: return@forEach
                    val applicationId = doc.id

                    db.collection("students")
                        .document(applicant.studentId)
                        .get()
                        .addOnSuccessListener { studentDoc ->

                            val student =
                                studentDoc.toObject(StudentModel::class.java) ?: return@addOnSuccessListener

                            result.add(applicationId to (applicant to student))

                            //  sort by appliedAt DESC (new first)
                            onResult(
                                result.sortedByDescending {
                                    it.second.first.appliedAt
                                }
                            )
                        }
                }
            }
    }


    //Archive jobs(soft delete)

    suspend fun archiveJob(jobId: String) {
        db.collection("jobs")
            .document(jobId)
            .update("isActive", false)
            .await()
    }

    suspend fun getStudentsByBatch(batch: String): List<StudentModel> {
        val snapshot = db.collection("students")
            .whereEqualTo("batchYear", batch)
            .get()
            .await()

        return snapshot.toObjects(StudentModel::class.java)
    }

    // to delete old data
    suspend fun deleteOldBatchData() {

        val allowed = BatchUtils.getAllowedBatches()

        // Delete old students
        val students = db.collection("students").get().await()
        for (doc in students.documents) {
            val batch = doc.getString("batchYear") ?: continue
            if (!allowed.contains(batch)) {
                doc.reference.delete()
            }
        }

        // Delete old jobs
        val jobs = db.collection("jobs").get().await()
        for (doc in jobs.documents) {
            val batch = doc.getString("batchYear") ?: continue
            if (!allowed.contains(batch)) {
                doc.reference.delete()
            }
        }
    }

    //Get placed students list

    suspend fun getPlacedStudentsWithCompanies(batch: String)
            : List<Pair<StudentModel, List<String>>> {

        val result = mutableListOf<Pair<StudentModel, List<String>>>()

        //  get students of batch
        val studentsSnapshot = db.collection("students")
            .whereEqualTo("batchYear", batch)
            .get()
            .await()

        for (studentDoc in studentsSnapshot.documents) {

            val student = studentDoc.toObject(StudentModel::class.java) ?: continue

//  match dashboard logic
            if (student.placementStatus != "PLACED") continue

            val studentId = studentDoc.id


            //  get placed applications of that student
            val apps = db.collection("applications")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("status", "PLACED")
                .get()
                .await()

            if (apps.isEmpty) continue

            val companies = mutableListOf<String>()

            //  get company from each job
            for (app in apps.documents) {

                val jobId = app.getString("jobId") ?: continue

                val jobDoc = db.collection("jobs")
                    .document(jobId)
                    .get()
                    .await()

                val company = jobDoc.getString("company") ?: continue
                companies.add(company)
            }

            result.add(student to companies.distinct())
        }

        return result
    }


    // to get company analytics..............................

    suspend fun getCompanyAnalytics(
        company: String,
        batch: String
    ): CompanyAnalytics {

        val jobs = db.collection("jobs")
            .whereEqualTo("company", company)
            .whereEqualTo("batchYear", batch)
            .get()
            .await()

        var totalApplicants = 0
        var placedCount = 0

        var role = ""
        var location = ""
        var status = ""

        for (job in jobs.documents) {

            role = job.getString("role") ?: ""
            location = job.getString("location") ?: ""
            status = job.getString("status") ?: ""

            val jobId = job.id

            val applications = db.collection("applications")
                .whereEqualTo("jobId", jobId)
                .get()
                .await()

            totalApplicants += applications.size()
            placedCount += applications.documents.count {
                it.getString("status") == "PLACED"
            }
        }

        val selectionRate =
            if (totalApplicants == 0) 0
            else (placedCount * 100) / totalApplicants

        return CompanyAnalytics(
            company,
            role,
            location,
            totalApplicants,
            placedCount,
            selectionRate,
            status
        )
    }


// to reflect instantly in same screen UI when the status job status is changed...........

    fun listenAllJobs(onUpdate: (List<JobModel>) -> Unit) {

        db.collection("jobs")
            .whereNotEqualTo("status", "Archived")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null) {
                    val jobs = snapshot.toObjects(JobModel::class.java)
                        .sortedByDescending { it.timestamp }

                    onUpdate(jobs)
                }
            }
    }

    //student dashboard..........

    suspend fun getCurrentStudentBatch(): String {
        val uid = auth.currentUser?.uid ?: return ""
        val doc = db.collection("students").document(uid).get().await()
        return doc.getString("batchYear") ?: ""
    }

    suspend fun getNoticesForBatch(batch: String): List<NoticeModel> {
        val snap = db.collection("notices")
            .whereEqualTo("batchYear", batch)
            .get()
            .await()

        return snap.toObjects(NoticeModel::class.java)
    }
//....................................
    suspend fun getCurrentStudent(): StudentModel {

        val uid = auth.currentUser?.uid ?: return StudentModel()

        val doc = db.collection("students")
            .document(uid)
            .get()
            .await()

        return doc.toObject(StudentModel::class.java) ?: StudentModel()
    }


    suspend fun getNoticesByBatch(batch: String): List<NoticeModel> {

        val snapshot = db.collection("notices")
            .whereEqualTo("batchYear", batch)
            .get()
            .await()

        return snapshot.toObjects(NoticeModel::class.java)
    }

    suspend fun getJobsByBatch(batch: String): List<JobModel> {

        val snapshot = db.collection("jobs")
            .whereEqualTo("isActive", true)
            .whereEqualTo("batchYear", batch)
            .get()
            .await()

        return snapshot.toObjects(JobModel::class.java)
            .sortedByDescending { it.timestamp }
    }


}
