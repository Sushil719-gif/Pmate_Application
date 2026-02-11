package com.example.pmate.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pmate.Firestore.FirestoreRepository.FirestoreRepository

class StudentViewModelFactory(
    private val repo: FirestoreRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StudentViewModel(repo) as T
    }
}
