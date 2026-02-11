package com.example.pmate.ui.Student.studentjobs.ApplyLogicFiles

import android.net.Uri
import com.example.pmate.Firestore.DataModels.StudentModel

object GoogleFormPrefillBuilder {

    fun buildLink(template: String, student: StudentModel): String {

        val base = template.substringBefore("?")

        return Uri.parse(base).buildUpon()
            .appendQueryParameter("entry.148622126", student.name)          // Name
            .appendQueryParameter("entry.559049739", student.email)         // Email
            .appendQueryParameter("entry.1169676122", student.usn)          // USN
            .appendQueryParameter("entry.599313694", student.branch)        // Branch
            .appendQueryParameter("entry.1726474439", student.cgpa.toString()) // CGPA
            .appendQueryParameter("entry.XXXXXXXXXX", student.gender)      // Gender
            .appendQueryParameter("entry.YYYYYYYYYY", student.phone)       // Phone
            .build()
            .toString()
    }
}
