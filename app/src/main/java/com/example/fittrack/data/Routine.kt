package com.example.fittrack.data

data class Routine(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val daysOfWeek: List<String> = emptyList(),  // Lista de días de entrenamiento
    val restDays: List<String> = emptyList(),    // Lista de días de descanso
    val exercisesByDay: Map<String, List<ExerciseConfig>> = emptyMap(), // Mapa de día -> lista de ejercicios
    val dayNames: Map<String, String> = emptyMap(), // NUEVO: Mapa de día -> nombre personalizado
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
