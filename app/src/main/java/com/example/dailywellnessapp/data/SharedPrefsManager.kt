package com.example.dailywellnessapp.data

import android.content.Context
import com.example.dailywellnessapp.model.Habit
import com.example.dailywellnessapp.model.MoodEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences("HabitPrefs", Context.MODE_PRIVATE)

    fun saveHabits(habits: List<Habit>) {
        val json = Gson().toJson(habits)
        prefs.edit().putString("habit_list", json).apply()
    }

    fun loadHabits(): List<Habit> {
        val json = prefs.getString("habit_list", null)
        return if (json != null) {
            val type = object : TypeToken<List<Habit>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveMoodEntries(entries: List<MoodEntry>) {
        val json = Gson().toJson(entries)
        prefs.edit().putString("mood_entries", json).apply()
    }

    fun loadMoodEntries(): List<MoodEntry> {
        val json = prefs.getString("mood_entries", null)
        return if (json != null) {
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveHydrationInterval(hours: Int) {
        prefs.edit().putInt("hydration_interval", hours).apply()
    }

    fun getHydrationInterval(): Int {
        return prefs.getInt("hydration_interval", 2) // default 2 hours
    }
}