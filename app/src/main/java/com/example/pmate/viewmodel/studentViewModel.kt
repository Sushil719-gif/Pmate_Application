package com.example.pmate.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pmate.Firestore.DataModels.StudentModel
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentViewModel(
    private val repo: FirestoreRepository
) : ViewModel() {

    private val _student = MutableStateFlow<StudentModel?>(null)
    val student: StateFlow<StudentModel?> = _student

    fun loadStudent(studentId: String) {
        viewModelScope.launch {
            _student.value = repo.getStudentById(studentId)
        }
    }
}

