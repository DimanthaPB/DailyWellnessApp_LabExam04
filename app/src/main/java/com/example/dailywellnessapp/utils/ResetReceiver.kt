package com.example.dailywellnessapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.dailywellnessapp.data.SharedPrefsManager

class ResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = SharedPrefsManager(context)
        val habits = prefs.loadHabits().map { it.copy(isCompleted = false) }
        prefs.saveHabits(habits)
    }
}