package com.example.fittrack.data
import com.example.fittrack.data.Set

data class ExerciseRecord(
    val id: String = "",
    val exerciseId: String = "",
    val sets: List<com.example.fittrack.data.Set> = emptyList(),
    val date: Long = 0
)