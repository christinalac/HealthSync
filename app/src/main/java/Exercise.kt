package com.example.healthsync



data class Exercise (
     val name: String,
     val category: String,
     val setsReps: String,
     var isChecked: Boolean = false
)
