package com.example.fittrack.data

data class ExerciseWithSets(
    val exercise: Exercise,
    val sets: List<com.example.fittrack.data.Set> = emptyList()
)

