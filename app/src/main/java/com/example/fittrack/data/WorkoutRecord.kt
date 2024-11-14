package com.example.fittrack.data

data class WorkoutRecord(
    val id: String = "",  // Identificador único del registro
    val userId: String = "",  // ID del usuario que realiza el entrenamiento
    val routineId: String = "",  // ID de la rutina asociada al entrenamiento
    val exerciseRecords: List<String> = emptyList(),  // Lista de IDs de los ExerciseRecord relacionados con este entrenamiento
    val date: Long = System.currentTimeMillis(),  // Fecha y hora del entrenamiento
    val notes: String = ""  // Notas adicionales sobre el entrenamiento
)