package com.example.fittrack.data

data class WorkoutRecord(
    val id: String = "",
    val userId: String = "",
    val routineId: String = "",
    val exerciseRecords: List<String> = emptyList(),
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)