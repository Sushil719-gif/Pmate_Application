package com.example.pmate.Auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutConfirmScreen(navController: NavController) {

    val context = LocalContext.current
    val session = SessionManager(context)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Logout") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Are you sure you want to logout?",
                fontSize = 20.sp
            )

            Spacer(Modifier.height(30.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.width(130.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        scope.launch {
                            session.clearSession()
                            navController.navigate("role_selection") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.width(130.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Logout", color = Color.White)
                }
            }
        }
    }
}
