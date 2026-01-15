package com.example.pmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.pmate.Navigation.AppNavigation
import com.example.pmate.data.ThemePreferences
import com.example.pmate.ui.theme.MyAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val themePref = remember { ThemePreferences(context) }
            val isDarkMode by themePref.isDarkMode.collectAsState(initial = false)

            MyAppTheme(darkTheme = isDarkMode) {
                AppNavigation(navController)
            }
        }

    }
}
