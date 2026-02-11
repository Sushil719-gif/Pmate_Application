package com.example.pmate.Firestore.DataModels

data class CsvUndoRecord(
    val usn: String = "",
    val oldName: String = "",
    val oldCgpa: Double = 0.0,
    val oldBacklogs: Int = 0
)

data class CsvUploadHistory(
    val uploadId: String = "",
    val batch: String = "",
    val timestamp: Long = 0L,
    val expiresAt: Long = 0L,
    val records: List<CsvUndoRecord> = emptyList()
)

data class CsvUploadResult(
    val uploadId: String,
    val updated: Int,
    val skipped: Int,
    val notFound: Int,
    val expiresAt: Long
)
