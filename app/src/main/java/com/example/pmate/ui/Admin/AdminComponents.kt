package com.example.pmate.ui.Admin



import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import java.util.Calendar

// -------------------------------
// 1️⃣ Shared Input Field
// -------------------------------
@Composable
fun AdminInputField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

// -------------------------------
// 2️⃣ Shared Date Picker
// -------------------------------
fun adminPickDate(context: Context, onSelected: (String) -> Unit) {
    val c = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, y, m, d -> onSelected("$d/${m + 1}/$y") },
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH),
        c.get(Calendar.DAY_OF_MONTH)
    ).show()
}
