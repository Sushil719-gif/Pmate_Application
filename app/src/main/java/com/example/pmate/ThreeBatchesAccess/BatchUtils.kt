package com.example.pmate.ThreeBatchesAccess



import java.util.Calendar

object BatchUtils {

    fun getAllowedBatches(): List<String> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        return listOf(
            (currentYear - 1).toString(), // previous
            currentYear.toString(),       // current
            (currentYear + 1).toString() // next
        )
    }

    fun isAllowedBatch(batch: String): Boolean {
        return getAllowedBatches().contains(batch)
    }
}
