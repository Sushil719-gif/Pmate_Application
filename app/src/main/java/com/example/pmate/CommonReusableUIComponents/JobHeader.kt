package com.example.pmate.CommonReusableUIComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===================== Helper UI =====================

@Composable
fun JobHeader(
    company: String,
    role: String,
    batch: String,
    deadline: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Top — Company
            Text(
                text = company,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            // Middle — Role
            Text(
                text = role,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Bottom — Chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                AssistChip(
                    onClick = { },
                    label = { Text("Deadline:\n $deadline") }
                )

                AssistChip(
                    onClick = { },
                    label = { Text("$batch Batch") }
                )
            }

        }
    }
}
