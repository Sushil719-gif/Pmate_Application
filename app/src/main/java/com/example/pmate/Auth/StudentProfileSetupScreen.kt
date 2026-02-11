package com.example.pmate.Auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pmate.Firestore.DataModels.StudentModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun StudentProfileSetupScreen(
    repo: FirestoreRepository,
    student: StudentModel,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uid = FirebaseAuth.getInstance().currentUser!!.uid
    val email = FirebaseAuth.getInstance().currentUser!!.email ?: ""

    var name by remember { mutableStateOf(student.name) }
    var usn by remember { mutableStateOf(student.usn) }
    var branch by remember { mutableStateOf(student.branch) }
    var batch by remember { mutableStateOf(student.batchYear) }
    var gender by remember { mutableStateOf(student.gender) }
    var phone by remember { mutableStateOf(student.phone) }


    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
        ,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Text("Complete Your Profile", fontSize = 22.sp)

        OutlinedTextField(
            value = email,
            onValueChange = {},
            readOnly = true,
            label = { Text("College Email") },
            modifier = Modifier.fillMaxWidth()
        )


        OutlinedTextField(name, { name = it }, label = { Text("Full Name") })
        OutlinedTextField(usn, { usn = it }, label = { Text("USN") })
        OutlinedTextField(branch, { branch = it }, label = { Text("Branch") })
        OutlinedTextField(batch, { batch = it }, label = { Text("Batch Year") })
        OutlinedTextField(phone, { phone = it }, label = { Text("Phone Number") })

        Text("Gender")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row {
                RadioButton(gender == "Male", { gender = "Male" })
                Text("Male")
            }
            Row {
                RadioButton(gender == "Female", { gender = "Female" })
                Text("Female")
            }
            Row {
                RadioButton(gender == "Others", { gender = "Others" })
                Text("Others")
            }
        }


        Button(
            onClick = {
                scope.launch {
                    repo.updateStudentProfile(
                        uid, name, usn, branch, batch, gender, phone
                    )
                    onDone()
                }
            },
            enabled = name.isNotBlank() &&
                    usn.isNotBlank() &&
                    branch.isNotBlank() &&
                    batch.isNotBlank() &&
                    gender.isNotBlank() &&
                    phone.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save & Continue")
        }
    }
}
