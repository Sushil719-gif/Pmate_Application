package com.example.pmate.ui.Admin.dashboard.company



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun CompanyStatusScreen(
    navController: NavController,
    name: String,
    currentStatus: String
) {

    var status by remember { mutableStateOf(currentStatus) }

    val statusOptions = listOf("Active", "On Hold", "Completed")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Header
        Text(
            text = name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Company Status",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            statusOptions.forEach { option ->
                FilterChip(
                    selected = status == option,
                    onClick = { status = option },
                    label = { Text(option) }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // If completed → show placement details button
        if (status == "Completed") {
            Button(
                onClick = {
                    navController.navigate("CompanyDetails/$name")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("View Placement Details")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save button (later we update Firestore here)
        Button(
            onClick = {
                // TODO: Update Firestore company status
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Save")
        }
    }
}
