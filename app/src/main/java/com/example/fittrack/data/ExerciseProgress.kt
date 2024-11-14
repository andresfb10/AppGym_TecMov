package com.example.fittrack.data

data class ExerciseProgress(
    val exerciseId: String = "",
    val maxWeight: Float = 0f,
    val maxReps: Int = 0,
    val totalVolume: Float = 0f,
    val lastWorkoutDate: Long = 0
)