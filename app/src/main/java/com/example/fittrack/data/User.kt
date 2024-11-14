package com.example.fittrack.data

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val birthDate: String = "",
    var imageUrl: String = ""
)