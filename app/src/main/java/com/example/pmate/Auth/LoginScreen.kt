package com.example.pmate.Auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    expectedRole: String
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val session = LocalSessionManager.current

    val repo = remember { FirestoreRepository(session) }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                errorMessage = ""

                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill all fields"
                    return@Button
                }

                isLoading = true

                auth.signInWithEmailAndPassword(
                    email.trim(),
                    password.trim()
                ).addOnSuccessListener { result ->

                    val uid = result.user!!.uid

                    scope.launch {
                        val user = repo.getUserById(uid)
                        isLoading = false

                        if (user == null) {
                            errorMessage = "User profile not found"
                            auth.signOut()
                            return@launch
                        }

                        val actualRole = user.role.lowercase()
                        val expected = expectedRole.lowercase()

                        if (actualRole != expected) {
                            errorMessage =
                                "You are trying to log in as $expectedRole but this account belongs to $actualRole"
                            auth.signOut()
                            return@launch
                        }

                        // Auto-create student profile
                        if (actualRole == "student") {
                            repo.createStudentFromUserIfNotExists(
                                uid = uid,
                                user = user
                            )
                        }

                        // ✅ Save session → Load collegeId → Navigate (same coroutine)
                        session.saveUserSession(
                            userId = uid,
                            email = email,
                            role = user.role,
                            collegeId = user.collegeId
                        )

                        session.loadCollegeId()

                        Log.d("SAAS_TEST", "CollegeId = ${session.currentCollegeId}")

                        if (actualRole == "admin") {
                            navController.navigate("admin_main") {
                                popUpTo("login/$expectedRole") { inclusive = true }
                            }
                        } else {
                            navController.navigate("student_main") {
                                popUpTo("login/$expectedRole") { inclusive = true }
                            }
                        }
                    }

                }.addOnFailureListener {
                    isLoading = false
                    errorMessage = "Invalid email or password"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Login", fontSize = 16.sp)
            }
        }
    }
}
