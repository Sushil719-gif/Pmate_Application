package com.example.pmate.Firestore.FirestoreRepository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.pmate.Firestore.DataModels.Applicant
import com.example.pmate.Firestore.DataModels.ApplicationModel
import com.example.pmate.Firestore.DataModels.CompanyAnalytics
import com.example.pmate.Firestore.DataModels.CsvUndoRecord
import com.example.pmate.Firestore.DataModels.CsvUploadHistory
import com.example.pmate.Firestore.DataModels.CsvUploadResult
import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.DataModels.NoticeModel
import com.example.pmate.Firestore.DataModels.StudentModel
import com.example.pmate.Firestore.DataModels.UserModel
import com.example.pmate.ThreeBatchesAccess.BatchUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import kotlinx.coroutines.tasks.await
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Calendar
import java.util.UUID
import com.example.pmate.Auth.SessionManager
import com.example.pmate.Firestore.DataModels.OADetails
import com.example.pmate.SaasManagement.FirestorePaths


class FirestoreRepository(
    private val sessionManager: SessionManager
) {

    private val auth = FirebaseAuth.getInstance()
    private fun collegeId(): String {
        return sessionManager.currentCollegeId
    }


    // ADD JOB (ADMIN)
    suspend fun addJob(job: JobModel): String? {
        return try {
            val docRef = FirestorePaths.jobs(collegeId())
                .document()
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

        val snapshot = FirestorePaths.jobs(collegeId())

            .whereNotEqualTo("status", "Archived")   // only hide archived
            .get()
            .await()

        return snapshot.toObjects(JobModel::class.java)
            .sortedByDescending { it.timestamp }
    }





    // FETCH JOB BY ID
    suspend fun getJobById(jobId: String): JobModel? {
        return try {
            FirestorePaths.jobs(collegeId())

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
            val alreadyApplied = FirestorePaths.applications(collegeId())

                .whereEqualTo("jobId", jobId)
                .whereEqualTo("studentId", studentId)
                .get()
                .await()

            if (!alreadyApplied.isEmpty) return false

            FirestorePaths.applications(collegeId())
                .add(
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
            FirestorePaths.applications(collegeId())

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
            val doc = FirestorePaths.students(collegeId())

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
            val doc = FirestorePaths.users(collegeId())

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
        val snapshot = FirestorePaths.applications(collegeId())

            .whereEqualTo("jobId", jobId)
            .whereEqualTo("studentId", studentId)
            .get()
            .await()

        return !snapshot.isEmpty
    }



    // UPDATE JOB
    suspend fun updateJob(job: JobModel): Boolean {
        return try {
            FirestorePaths.jobs(collegeId())
                .document(job.jobId).set(job).await()
            true
        } catch (e: Exception) {
            false
        }
    }


 //To Update Job Status

    suspend fun updateJobStatus(jobId: String, newStatus: String) {
        FirestorePaths.jobs(collegeId())

            .document(jobId)
            .update("status", newStatus)
            .await()
    }

    //to get particular company important details

    suspend fun getJobByCompany(company: String): JobModel? {
        val snapshot = FirestorePaths.jobs(collegeId())

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
    )
    {

        val now = System.currentTimeMillis()

        val notice = NoticeModel(
            title = title,
            message = message,
            batch = batch,
            level = level,
            timestamp = now,
            validTill = now + (validDays * 24 * 60 * 60 * 1000L)
        )

        FirestorePaths.notices(collegeId())

            .add(notice)
            .await()
    }




    suspend fun getAllNotices(): List<NoticeModel> {

        val now = System.currentTimeMillis()

        return FirestorePaths.notices(collegeId())

            .get()
            .await()
            .toObjects(NoticeModel::class.java)
            .filter { it.validTill > now }   // hide expired notices
    }




    suspend fun getUserByEmailAndPassword(email: String, password: String): UserModel? {
        return try {
            val snapshot = FirestorePaths.users(collegeId())

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
            val snapshot = FirestorePaths.users(collegeId())

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

        val apps = FirestorePaths.applications(collegeId())

            .whereEqualTo("jobId", jobId)
            .get()
            .await()

        val result = mutableListOf<Pair<String, Pair<Applicant, StudentModel>>>()

        for (doc in apps.documents) {

            val applicant = doc.toObject(Applicant::class.java) ?: continue
            val studentDoc = FirestorePaths.students(collegeId())

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
        val updates = mutableMapOf<String, Any>(
            "status" to status
        )

        if (status != "SHORTLISTED") {
            updates["oaDetails"] =
                com.google.firebase.firestore.FieldValue.delete()
        }

        FirestorePaths.applications(collegeId())
            .document(applicationId)
            .update(updates)
            .await()

        val apps = FirestorePaths.applications(collegeId())
            .whereEqualTo("studentId", studentId)
            .get()
            .await()

        val isPlacedAnywhere = apps.documents.any {
            it.getString("status") == "PLACED"
        }

        FirestorePaths.students(collegeId())
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
        val docRef = FirestorePaths.students(collegeId())
            .document(uid)
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
        val docRef = FirestorePaths.students(collegeId())
            .document(uid)
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

        val snapshot = FirestorePaths.applications(collegeId())

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

    FirestorePaths.applications(collegeId())

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
        FirestorePaths.applications(collegeId())

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

                    FirestorePaths.students(collegeId())

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

    // permanent delete

    suspend fun deleteJobPermanently(jobId: String) {
        FirestorePaths.jobs(collegeId())

            .document(jobId)
            .delete()
            .await()
    }


    //Archive jobs(soft delete)

    suspend fun archiveJob(jobId: String) {
        FirestorePaths.jobs(collegeId())

            .document(jobId)
            .update("active", false)
            .await()
    }


    suspend fun getStudentsByBatch(batch: String): List<StudentModel> {
        val snapshot = FirestorePaths.students(collegeId())

            .whereEqualTo("batchYear", batch)
            .get()
            .await()

        return snapshot.toObjects(StudentModel::class.java)
    }

    // to delete old data
    suspend fun deleteOldBatchData() {

        val allowed = BatchUtils.getAllowedBatches()

        // Delete old students
        val students = FirestorePaths.students(collegeId())
            .get().await()
        for (doc in students.documents) {
            val batch = doc.getString("batchYear") ?: continue
            if (!allowed.contains(batch)) {
                doc.reference.delete()
            }
        }

        // Delete old jobs
        val jobs =FirestorePaths.jobs(collegeId())
            .get().await()
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
        val studentsSnapshot = FirestorePaths.students(collegeId())

            .whereEqualTo("batchYear", batch)
            .get()
            .await()

        for (studentDoc in studentsSnapshot.documents) {

            val student = studentDoc.toObject(StudentModel::class.java) ?: continue

//  match dashboard logic
            if (student.placementStatus != "PLACED") continue

            val studentId = studentDoc.id


            //  get placed applications of that student
            val apps =FirestorePaths.applications(collegeId())

                .whereEqualTo("studentId", studentId)
                .whereEqualTo("status", "PLACED")
                .get()
                .await()

            if (apps.isEmpty) continue

            val companies = mutableListOf<String>()

            //  get company from each job
            for (app in apps.documents) {

                val jobId = app.getString("jobId") ?: continue

                val jobDoc = FirestorePaths.jobs(collegeId())

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

        val jobs = FirestorePaths.jobs(collegeId())

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

            val applications = FirestorePaths.applications(collegeId())

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

        FirestorePaths.jobs(collegeId())

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
        val doc = FirestorePaths.students(collegeId())
            .document(uid).get().await()
        return doc.getString("batchYear") ?: ""
    }

    suspend fun getNoticesForBatch(batch: String): List<NoticeModel> {
        val snap = FirestorePaths.notices(collegeId())

            .whereEqualTo("batchYear", batch)
            .get()
            .await()

        return snap.toObjects(NoticeModel::class.java)
    }
//....................................
    suspend fun getCurrentStudent(): StudentModel {

        val uid = auth.currentUser?.uid ?: return StudentModel()

        val doc = FirestorePaths.students(collegeId())

            .document(uid)
            .get()
            .await()

        return doc.toObject(StudentModel::class.java) ?: StudentModel()
    }


    suspend fun getNoticesByBatch(batch: String): List<NoticeModel> {

        val snapshot = FirestorePaths.notices(collegeId())

            .whereEqualTo("batchYear", batch)
            .get()
            .await()

        return snapshot.toObjects(NoticeModel::class.java)
    }

    suspend fun getJobsByBatch(batch: String): List<JobModel> {

        val snapshot = FirestorePaths.jobs(collegeId())

            .whereEqualTo("isActive", true)
            .whereEqualTo("batchYear", batch)
            .get()
            .await()

        return snapshot.toObjects(JobModel::class.java)
            .sortedByDescending { it.timestamp }
    }


    // Importing student data

    suspend fun processResultCsv(
        context: Context,
        uri: Uri,
        selectedBatch: String
    ): CsvUploadResult {

        val undoRecords = mutableListOf<CsvUndoRecord>()

        val inputStream = context.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val lines = reader.readLines()
        reader.close()

        var updated = 0
        var skipped = 0
        var notFound = 0

        for (i in 1 until lines.size) {

            val parts = lines[i].split(",")

            if (parts.size < 4) continue

            val name = parts[0].trim()
            val usn = parts[1].trim()
            val cgpa = parts[2].trim().toDoubleOrNull() ?: continue
            val backlogs = parts[3].trim().toIntOrNull() ?: continue

            val (result, oldRecord) =
                updateStudentResult(usn, name, cgpa, backlogs, selectedBatch)

            when (result) {
                "UPDATED" -> {
                    updated++
                    oldRecord?.let { undoRecords.add(it) }
                }
                "SKIPPED" -> skipped++
                "NOT_FOUND" -> notFound++
            }
        }

        // ✅ Save undo history only once
        val uploadId = saveUndoHistory(selectedBatch, undoRecords)

        val expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000)

        return CsvUploadResult(
            uploadId = uploadId,
            updated = updated,
            skipped = skipped,
            notFound = notFound,
            expiresAt = expiresAt
        )
    }




    private suspend fun updateStudentResult(
        usn: String,
        name: String,
        cgpa: Double,
        backlogs: Int,
        selectedBatch: String
    ): Pair<String, CsvUndoRecord?> {

        val query = FirestorePaths.students(collegeId())

            .whereEqualTo("usn", usn)
            .whereEqualTo("batchYear", selectedBatch)
            .get()
            .await()

        if (query.isEmpty) return "NOT_FOUND" to null

        val doc = query.documents.first()

        val oldRecord = CsvUndoRecord(
            usn = usn,
            oldName = doc.getString("name") ?: "",
            oldCgpa = doc.getDouble("cgpa") ?: 0.0,
            oldBacklogs = (doc.getLong("backlogs") ?: 0L).toInt()
        )

        doc.reference.update(
            mapOf(
                "name" to name,
                "cgpa" to cgpa,
                "backlogs" to backlogs
            )
        )

        return "UPDATED" to oldRecord
    }

//save history of student details.......
private suspend fun saveUndoHistory(
    batch: String,
    records: List<CsvUndoRecord>
): String {

    val uploadId = UUID.randomUUID().toString()
    val now = System.currentTimeMillis()

    val history = CsvUploadHistory(
        uploadId = uploadId,
        batch = batch,
        timestamp = now,
        expiresAt = now + (24 * 60 * 60 * 1000),
        records = records
    )

    FirestorePaths.csvHistory(collegeId())

        .document(uploadId)
        .set(history)
        .await()

    return uploadId
}



    // undo function...........

    suspend fun undoCsvUpload(uploadId: String) {

        val historyDoc = FirestorePaths.csvHistory(collegeId())

            .document(uploadId)
            .get()
            .await()

        val history = historyDoc.toObject(CsvUploadHistory::class.java)
            ?: return

        for (record in history.records) {
            val query = FirestorePaths.students(collegeId())

                .whereEqualTo("usn", record.usn)
                .get()
                .await()

            for (doc in query.documents) {
                doc.reference.update(
                    mapOf(
                        "name" to record.oldName,
                        "cgpa" to record.oldCgpa,
                        "backlogs" to record.oldBacklogs
                    )
                )
            }
        }

        historyDoc.reference.delete()
    }

    suspend fun getLastCsvUploadForBatch(batch: String): CsvUploadResult? {
        val query = FirestorePaths.csvHistory(collegeId())

            .whereEqualTo("batch", batch)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        val doc = query.documents.firstOrNull() ?: return null
        return doc.toObject(CsvUploadResult::class.java)
    }


//Student's data collected once

    suspend fun updateStudentProfile(
        studentId: String,
        name: String,
        usn: String,
        branch: String,
        batch: String,
        gender: String,
        phone: String
    ) {
        FirestorePaths.students(collegeId())
            .document(studentId)
            .update(
                mapOf(
                    "name" to name,
                    "usn" to usn,
                    "branch" to branch,
                    "batchYear" to batch,
                    "gender" to gender,
                    "phone" to phone
                )
            ).await()
    }

//Add offers to students model

    suspend fun addOfferToStudent(studentId: String, lpa: Double) {

        val docRef = FirestorePaths.students(collegeId())
            .document(studentId)

        val snap = docRef.get().await()
        val student = snap.toObject(StudentModel::class.java) ?: return

        val updatedOffers = student.offers + lpa

        docRef.update(
            mapOf(
                "offers" to updatedOffers,
                "placementStatus" to "PLACED"
            )
        ).await()
    }



    suspend fun updatePlacementStatus(studentId: String, status: String) {

        FirestorePaths.students(collegeId())
            .document(studentId)
            .update("placementStatus", status)
            .await()
    }

    suspend fun updateJobFormId(jobId: String, jobFormId: String) {
        FirestorePaths.jobs(collegeId())
            .document(jobId)
            .update("jobFormId", jobFormId)
            .await()
    }

    suspend fun assignOAToApplicants(
        roomMap: Map<String, String>,
        oa: OADetails
    ) {
        roomMap.forEach { (applicationId, room) ->

            FirestorePaths.applications(collegeId())
                .document(applicationId)
                .update(
                    "oaDetails",
                    oa.copy(room = room)
                )
                .await()
        }
    }







}
