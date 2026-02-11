package com.example.pmate.ui.Student.studentjobs.ApplyLogicFiles

import com.example.pmate.Firestore.DataModels.JobModel
import com.example.pmate.Firestore.DataModels.StudentModel

object ApplyLogic {

//    fun getApplyState(
//        student: StudentModel,
//        job: JobModel,
//        alreadyApplied: Boolean
//    ): ApplyState {
//
//        if (alreadyApplied) return ApplyState.ALREADY_APPLIED
//
//        if (!job.active || job.status != "Active")
//            return ApplyState.JOB_CLOSED
//
//// check deadline only if timestamp is valid
//        if (job.deadlineTimestamp > 0 &&
//            System.currentTimeMillis() * 10 > job.deadlineTimestamp
//
//        ) return ApplyState.JOB_CLOSED
//
//        if (student.cgpa < job.minCgpa)
//            return ApplyState.CGPA_LOW
//
//        if (job.branches.isNotEmpty() && student.branch !in job.branches)
//
//
//        if (student.batchYear != job.batchYear)
//            return ApplyState.BATCH_MISMATCH
//
//        if (job.eligibilityType == "UNPLACED_ONLY"
//            && student.placementStatus != "IN_PROCESS"
//        ) return ApplyState.PLACEMENT_RESTRICTED
//
//        return ApplyState.CAN_APPLY
//    }


    fun getApplyState(
        student: StudentModel,
        job: JobModel,
        alreadyApplied: Boolean
    ): ApplyState {

        val highestOffer: Double = student.offers.maxOrNull() ?: 0.0


        if (alreadyApplied) return ApplyState.ALREADY_APPLIED

        if (!job.active || job.status != "Active")
            return ApplyState.JOB_CLOSED

        if (job.deadlineTimestamp != 0L &&
            System.currentTimeMillis() > job.deadlineTimestamp
        ) return ApplyState.JOB_CLOSED

        if (student.batchYear != job.batchYear)
            return ApplyState.BATCH_MISMATCH

        if (job.branches.isNotEmpty() && student.branch !in job.branches)
            return ApplyState.BRANCH_MISMATCH

        if (student.backlogs > 0)
            return ApplyState.BACKLOG_RESTRICTED

        if (student.cgpa < job.minCgpa)
            return ApplyState.CGPA_LOW

        if (job.eligibilityType == "FEMALE_ONLY" && student.gender != "Female")
            return ApplyState.GENDER_RESTRICTED

        if (job.eligibilityType == "UNPLACED_FEMALE_ONLY" &&
            (student.gender != "Female" || student.placementStatus != "IN_PROCESS")
        ) return ApplyState.GENDER_RESTRICTED

        if (job.isDreamJob &&
            student.placementStatus != "IN_PROCESS" &&
            highestOffer >= job.dreamPackageLimit
        ) return ApplyState.DREAM_PACKAGE_RESTRICTED



        if (job.eligibilityType == "UNPLACED_ONLY" &&
            student.placementStatus != "IN_PROCESS"
        ) return ApplyState.PLACEMENT_RESTRICTED

        return ApplyState.CAN_APPLY

    }


}

