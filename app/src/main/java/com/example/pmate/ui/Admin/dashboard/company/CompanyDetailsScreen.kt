package com.example.pmate.ui.Admin.dashboard.company


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class RoleInfo(
    val roleName: String,
    val packageInfo: String,
    val studentsPlaced: Int
)

@Composable
fun CompanyDetailsScreen(
    navController: NavController,
    name: String
) {

    // Dummy data for now (Firestore integration later)
    val roles = listOf(
        RoleInfo("Software Engineer", "6 LPA", 12),
        RoleInfo("System Engineer", "4 LPA", 8),
        RoleInfo("Intern", "15k/month", 4)
    )

    val totalPlaced = roles.sumOf { it.studentsPlaced }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Header
        Text(
            text = name,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Placement Summary",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Total placed card
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Total Students Placed",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = totalPlaced.toString(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Roles Offered",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(roles) { role -> RoleCard(role) }
        }
    }
}

@Composable
fun RoleCard(role: RoleInfo) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = role.roleName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Package: ${role.packageInfo}",
                fontSize = 14.sp
            )

            Text(
                text = "Students Placed: ${role.studentsPlaced}",
                fontSize = 14.sp
            )
        }
    }
}
