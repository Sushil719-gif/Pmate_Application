package com.example.pmate.ui.Admin.DynamicFormEngine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pmate.Firestore.FirestoreRepository.FormRepository

class TemplateBuilderVMFactory(
    private val repo: FormRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TemplateBuilderViewModel(repo) as T
    }
}
