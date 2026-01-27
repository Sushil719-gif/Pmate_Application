package com.example.pmate.ui.Student.studentsettings


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun StudentSettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))

        // ============================
        //     ACCOUNT SETTINGS
        // ============================
        SettingsSection(title = "Account") {

            SettingsItem(
                icon = Icons.Default.Person,
                title = "Edit Profile",
                onClick = { navController.navigate("update_profile") }
            )

            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Change Password",
                onClick = { navController.navigate("change_password") }
            )
        }

        Spacer(Modifier.height(24.dp))

        // ============================
        //     APP SETTINGS
        // ============================
        SettingsSection(title = "App Settings") {

            ToggleItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                checked = notificationsEnabled,
                onToggle = { notificationsEnabled = it }
            )

            ToggleItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                checked = darkModeEnabled,
                onToggle = { darkModeEnabled = it }
            )
        }

        Spacer(Modifier.height(24.dp))

        // ============================
        //     HELP & SUPPORT
        // ============================
        SettingsSection(title = "Help & Support") {

            SettingsItem(
                icon = Icons.Default.Help,
                title = "FAQs",
                onClick = { /* open FAQs */ }
            )

            SettingsItem(
                icon = Icons.Default.Email,
                title = "Contact TPO",
                onClick = { /* open email screen */ }
            )
        }

        Spacer(Modifier.height(24.dp))

        // ============================
        //     ABOUT & LOGOUT
        // ============================
        SettingsSection(title = "General") {

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About App",
                onClick = { navController.navigate("about_app") }
            )

            SettingsItem(
                icon = Icons.Default.Logout,
                title = "Logout",
                titleColor = Color.Red,
                iconTint = Color.Red,
                onClick = { navController.navigate("logout_confirm") }
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = Color.Black,
    iconTint: Color = Color.Gray,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(icon, contentDescription = null, tint = iconTint)

        Spacer(Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = titleColor
        )
    }
}

@Composable
fun ToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(Modifier.width(16.dp))

        Text(title, fontSize = 16.sp)

        Spacer(Modifier.weight(1f))

        Switch(
            checked = checked,
            onCheckedChange = onToggle
        )
    }
}
