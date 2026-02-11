package com.example.pmate.Firestore.FirestoreRepository

import android.util.Log
import com.example.pmate.Firestore.DataModels.FormField
import com.example.pmate.Firestore.DataModels.FormTemplate
import com.example.pmate.Firestore.DataModels.JobForm
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FormRepository(private val collegeId: String) {

    private val db = FirebaseFirestore.getInstance()

    private fun collegeRoot() =
        db.collection("colleges").document(collegeId)

    // ---------------------------------------------------

    fun createTemplate(title: String, onDone: (String) -> Unit) {
        val templateId = collegeRoot()
            .collection("form_templates")
            .document().id

        val template = FormTemplate(
            templateId = templateId,
            title = title
        )

        collegeRoot()
            .collection("form_templates")
            .document(templateId)
            .set(template)
            .addOnSuccessListener { onDone(templateId) }
    }

    fun getTemplates(onResult: (List<FormTemplate>) -> Unit) {
        collegeRoot()
            .collection("form_templates")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(FormTemplate::class.java)
                } ?: emptyList()
                onResult(list)
            }
    }

    fun addFieldToTemplate(templateId: String, field: FormField) {
        collegeRoot()
            .collection("form_templates")
            .document(templateId)
            .collection("fields")
            .document(field.fieldId)
            .set(field, SetOptions.merge())

    }

    fun getTemplateFields(
        templateId: String,
        onResult: (List<FormField>) -> Unit
    ) {
        collegeRoot()
            .collection("form_templates")
            .document(templateId)
            .collection("fields")
            .orderBy("order")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(FormField::class.java)
                } ?: emptyList()
                onResult(list)
            }
    }

    fun getJobFormTitle(
        jobFormId: String,
        onResult: (String) -> Unit
    ) {
        collegeRoot()
            .collection("job_forms")
            .document(jobFormId)
            .get()
            .addOnSuccessListener {
                val title = it.getString("title") ?: "Form"
                onResult(title)
            }
    }

    fun getJobFormFields(
        jobFormId: String,
        onResult: (List<FormField>) -> Unit
    ) {
        db.collection("colleges")
            .document(collegeId)              //  THIS makes it college aware
            .collection("job_forms")
            .document(jobFormId)
            .collection("fields")
            .orderBy("order")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(FormField::class.java)
                } ?: emptyList()
                onResult(list)
            }
    }



    suspend fun submitApplication(
        jobId: String,
        studentId: String,
        answers: Map<String, String>
    ) {
        db.collection("colleges")
            .document(collegeId)
            .collection("applications")
            .document(jobId)
            .collection("students")
            .document(studentId)
            .set(
                mapOf(
                    "answers" to answers,
                    "appliedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    fun deleteFieldFromTemplate(
        templateId: String,
        fieldId: String
    ) {
        db.collection("colleges")
            .document(collegeId)
            .collection("form_templates")
            .document(templateId)
            .collection("fields")
            .document(fieldId)
            .delete()
    }

    fun deleteTemplate(
        templateId: String,
        onDone: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val templateRef = db.collection("colleges")
            .document(collegeId)
            .collection("form_templates")
            .document(templateId)

        templateRef.collection("fields")
            .get()
            .addOnSuccessListener { snap ->

                val batch = db.batch()

                // delete all fields
                snap.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                // delete template doc
                batch.delete(templateRef)

                batch.commit()
                    .addOnSuccessListener { onDone() }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }

    fun getTemplateTitle(
        templateId: String,
        onResult: (String) -> Unit
    ) {
        db.collection("colleges")
            .document(collegeId)
            .collection("form_templates")
            .document(templateId)
            .get()
            .addOnSuccessListener {
                onResult(it.getString("title") ?: "")
            }
    }

    fun updateTemplateTitle(
        templateId: String,
        newTitle: String
    ) {
        db.collection("colleges")
            .document(collegeId)
            .collection("form_templates")
            .document(templateId)
            .update("title", newTitle)
    }












    // ---------------------------------------------------

    fun copyTemplateToJobForm(
        templateId: String,
        jobId: String,
        onDone: (String) -> Unit
    ) {

        val jobFormId = collegeRoot()
            .collection("job_forms")
            .document().id

        collegeRoot()
            .collection("form_templates")
            .document(templateId)
            .get()
            .addOnSuccessListener { templateDoc ->

                val title = templateDoc.getString("title") ?: "Job Form"

                val jobForm = JobForm(
                    jobFormId = jobFormId,
                    jobId = jobId,
                    title = title
                )

                // Create job_form
                collegeRoot()
                    .collection("job_forms")
                    .document(jobFormId)
                    .set(jobForm)

                // Copy fields
                collegeRoot()
                    .collection("form_templates")
                    .document(templateId)
                    .collection("fields")
                    .get()
                    .addOnSuccessListener { fieldSnap ->

                        for (doc in fieldSnap.documents) {
                            val field = doc.toObject(FormField::class.java)
                            if (field != null) {

                                val updatedField = field.copy(
                                    sourceTemplateId = templateId
                                )

                                collegeRoot()
                                    .collection("job_forms")
                                    .document(jobFormId)
                                    .collection("fields")
                                    .document(field.fieldId)
                                    .set(updatedField)
                            }
                        }


                        // Update job with jobFormId
                        collegeRoot()
                            .collection("jobs")
                            .document(jobId)
                            .update("jobFormId", jobFormId)

                        onDone(jobFormId)
                    }
            }

    }
}
