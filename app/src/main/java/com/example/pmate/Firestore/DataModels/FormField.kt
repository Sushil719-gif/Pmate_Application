package com.example.pmate.Firestore.DataModels

data class FormField(
    val fieldId: String = "",
    val label: String = "",
    val type: String = "",
    val required: Boolean = false,
    val order: Int = 0,
    val options: List<String> = emptyList(),
    val placeholder: String = "",
    val sourceTemplateId: String = ""

)

