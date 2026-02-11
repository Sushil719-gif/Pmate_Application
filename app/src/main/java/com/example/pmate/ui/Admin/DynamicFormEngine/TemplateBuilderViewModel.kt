package com.example.pmate.ui.Admin.DynamicFormEngine

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.pmate.Firestore.DataModels.FormField
import com.example.pmate.Firestore.FirestoreRepository.FormRepository

class TemplateBuilderViewModel(
    private val repo: FormRepository
) : ViewModel() {

    var fields = mutableStateListOf<FormField>()
        private set

    var editingField by mutableStateOf<FormField?>(null)
        private set

    fun loadTemplateFields(templateId: String) {
        Log.d("TEMPLATE_DEBUG", "VM loading fields for $templateId")
        repo.getTemplateFields(templateId) { list ->
            Log.d("TEMPLATE_DEBUG", "Fields received = ${list.size}")
            fields.clear()
            fields.addAll(list.sortedBy { it.order })
        }
    }

    fun startEditing(field: FormField) {
        editingField = field
    }

    fun clearEditing() {
        editingField = null
    }

    fun addOrUpdateField(field: FormField) {
        val index = fields.indexOfFirst { it.fieldId == field.fieldId }

        if (index == -1) {
            // NEW FIELD
            fields.add(field.copy(order = fields.size + 1))
        } else {
            // EDIT FIELD → KEEP ORIGINAL ORDER
            val originalOrder = fields[index].order
            fields[index] = field.copy(order = originalOrder)
        }

        editingField = null
    }


    fun deleteField(field: FormField) {
        fields.remove(field)
        fields.forEachIndexed { index, f ->
            fields[index] = f.copy(order = index + 1)
        }
    }



    fun saveAll(templateId: String) {
        fields.forEach {
            repo.addFieldToTemplate(templateId, it)
        }
    }

    fun moveFieldUp(field: FormField) {
        val index = fields.indexOf(field)
        if (index > 0) {
            fields.removeAt(index)
            fields.add(index - 1, field)
        }
         

    }

    fun moveFieldDown(field: FormField) {
        val index = fields.indexOf(field)
        if (index < fields.size - 1) {
            fields.removeAt(index)
            fields.add(index + 1, field)
        }
    }



}
