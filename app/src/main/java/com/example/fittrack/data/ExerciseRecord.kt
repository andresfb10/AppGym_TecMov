package com.example.fittrack.data

data class ExerciseRecord(
    val id: String = "",
    val exerciseId: String = "",
    val sets: List<Set<Any?>> = emptyList(),
    val date: Long = System.currentTimeMillis()
)