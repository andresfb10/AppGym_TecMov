package com.example.fittrack.data

data class DayExercise(
    val exerciseId: String,
    val order: Int,         // Para mantener el orden de los ejercicios
    val sets: Int = 3,      // Número de series por defecto
    val repsPerSet: Int = 12 // Repeticiones por serie por defecto
)