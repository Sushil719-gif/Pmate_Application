package com.example.pmate.Firestore.DataModels



data class NoticeModel(
    val title: String = "",
    val message: String = "",
    val batch: String = "",
    val level: String = "INFO" , // INFO | IMPORTANT | CRITICAL

    val timestamp: Long = System.currentTimeMillis(),
    val validTill: Long = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000) // 3 days
)

