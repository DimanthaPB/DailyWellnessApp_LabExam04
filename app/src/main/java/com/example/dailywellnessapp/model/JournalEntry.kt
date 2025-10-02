package com.example.dailywellnessapp.model

data class JournalEntry(
    val id: Int,
    var mood: String,
    var note: String,
    val timestamp: Long
)