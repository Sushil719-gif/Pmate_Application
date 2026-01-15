package com.example.pmate.ui.Role

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoleSelectionScreen(
    onAdminClick: () -> Unit,
    onStudentClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B2A), // Dark Blue/Navy
                        Color(0xFF1B263B), // Deep Blue
                        Color(0xFF415A77), // Muted Blue
                        Color(0xFF778DA9) // Soft Blue/Grey
                    )
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Continue As",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        RoleCard(
            title = "Admin",
            icon = Icons.Default.AdminPanelSettings,
            color = Color(0xFF1F4287),
            onClick = onAdminClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        RoleCard(
            title = "Student",
            icon = Icons.Default.Person,
            color = Color(0xFF4A4E69),// normal button color
            onClick = onStudentClick
        )
    }
}

@SuppressLint("RememberInComposition")
@Composable
fun RoleCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .animateContentSize()
            .clickable(
                indication = null,
                interactionSource = MutableInteractionSource()
            ) {
                pressed = true
                onClick()
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF415A77)),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = title,
                fontSize = 22.sp,
                color = Color.White
            )
        }
    }
}
