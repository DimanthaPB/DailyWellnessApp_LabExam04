package com.example.dailywellnessapp.model

data class Habit(
    val id: Int,
    var name: String,
    var category: String,
    var isCompleted: Boolean = false,
    val isHeader: Boolean = false
)