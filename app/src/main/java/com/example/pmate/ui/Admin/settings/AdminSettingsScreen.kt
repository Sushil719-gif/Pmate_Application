package com.example.pmate.ui.Admin.settings



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.data.ThemePreferences


import kotlinx.coroutines.launch

@Composable
fun AdminSettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    val context = LocalContext.current
    val themePref = remember { ThemePreferences(context) }
    val scope = rememberCoroutineScope()

    // observe stored theme
    val isDarkTheme by themePref.isDarkMode.collectAsState(initial = false)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(20.dp)
    ) {

        Text(
            text = "Settings",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // ------------------ Account ------------------
        SettingsSectionTitle("Account")

        SettingsOptionCard(
            title = "Update Profile",
            subtitle = "Change your name, email, contact info",
            icon = Icons.Default.Person,
            onClick = { navController.navigate("update_profile") }
        )

        Spacer(Modifier.height(20.dp))

        SettingsOptionCard(
            title = "Change Password",
            subtitle = "Update your account password",
            icon = Icons.Default.Lock,
            onClick = { navController.navigate("change_password") }
        )

        Spacer(Modifier.height(20.dp))

        // ------------------ Preferences ------------------
        SettingsSectionTitle("Preferences")

        ThemeToggleCard(
            title = "Dark Mode",
            checked = isDarkTheme,
            onToggle = { enabled ->
                scope.launch {
                    themePref.setDarkMode(enabled)
                }
            }
        )

        Spacer(Modifier.height(20.dp))

        // ------------------ General ------------------
        SettingsSectionTitle("General")

        SettingsOptionCard(
            title = "About App",
            subtitle = "Learn more about this application",
            icon = Icons.Default.Info,
            onClick = { navController.navigate("about_app") }
        )

        Spacer(Modifier.height(20.dp))

        SettingsOptionCard(
            title = "Logout",
            subtitle = "Sign out from admin account",
            icon = Icons.Default.Logout,
            onClick = { navController.navigate("logout_confirm") }
        )
    }
}



// ---------------------------------------------------------------
// Reusable Components
// ---------------------------------------------------------------

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.DarkGray,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = Color(0xFF6750A4),
    titleColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(Color(0xFFEDE7F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = titleColor)
                Text(subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ThemeToggleCard(
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(Color(0xFFEDE7F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color(0xFF6750A4))
            }

            Spacer(Modifier.width(16.dp))

            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.weight(1f))

            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}
