package com.example.pmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.pmate.Auth.SessionManager
import com.example.pmate.Navigation.AppNavigation
import com.example.pmate.data.ThemePreferences
import com.example.pmate.ui.theme.MyAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val session = SessionManager(context)

            LaunchedEffect(Unit) {
                val loggedIn = session.isLoggedIn()
                val role = session.getUserRole()

                if (loggedIn) {
                    if (role == "admin") {
                        navController.navigate("admin_main") {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate("student_main") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }

            AppNavigation(navController)
        }


    }
}
