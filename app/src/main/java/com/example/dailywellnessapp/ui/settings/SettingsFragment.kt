package com.example.dailywellnessapp.ui.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dailywellnessapp.R
import com.example.dailywellnessapp.data.SharedPrefsManager
import com.example.dailywellnessapp.utils.HydrationReceiver

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var prefs: SharedPrefsManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = SharedPrefsManager(requireContext())

        val spinner = view.findViewById<Spinner>(R.id.intervalSpinner)
        val button = view.findViewById<Button>(R.id.btnSetReminder)

        val intervals = listOf(1, 2, 3, 4, 6)
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervals)

        button.setOnClickListener {
            val selected = spinner.selectedItem as Int
            prefs.saveHydrationInterval(selected)
            scheduleHydrationReminder(selected)
            Toast.makeText(requireContext(), "Reminder set every $selected hours", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleHydrationReminder(hours: Int) {
        val intent = Intent(requireContext(), HydrationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intervalMillis = hours * 60 * 60 * 1000L

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
    }
}
