package com.example.dailywellnessapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HydrationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationHelper(context).showHydrationNotification()
    }
}