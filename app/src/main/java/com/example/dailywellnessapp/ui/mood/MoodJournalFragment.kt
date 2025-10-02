package com.example.dailywellnessapp.ui.mood

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailywellnessapp.R
import com.example.dailywellnessapp.data.SharedPrefsManager
import com.example.dailywellnessapp.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

class MoodJournalFragment : Fragment(R.layout.fragment_mood_journal) {
    private lateinit var prefs: SharedPrefsManager
    private val moodEntries = mutableListOf<MoodEntry>()
    private lateinit var adapter: MoodAdapter

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastShakeTime = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = SharedPrefsManager(requireContext())
        moodEntries.addAll(prefs.loadMoodEntries())

        adapter = MoodAdapter(moodEntries)
        val recyclerView = view.findViewById<RecyclerView>(R.id.moodRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        view.findViewById<Button>(R.id.btnLogMood).setOnClickListener {
            showEmojiPicker()
        }

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
            }.timeInMillis

            val nextDay = selectedDate + 86400000 // +1 day in ms

            val filtered = moodEntries.filter {
                it.timestamp in selectedDate until nextDay
            }

            adapter = MoodAdapter(filtered)
            recyclerView.adapter = adapter
        }

        view.findViewById<Button>(R.id.btnShareMood).setOnClickListener {
            val summary = moodEntries.take(5).joinToString("\n") {
                val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(it.timestamp))
                "$date — ${it.emoji}"
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "My recent moods:\n$summary")
            }
            startActivity(Intent.createChooser(intent, "Share via"))
        }

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.let {
            sensorManager.registerListener(shakeListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private val shakeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val values = event?.values ?: return
            val x = values[0]
            val y = values[1]
            val z = values[2]

            val acceleration = sqrt(x * x + y * y + z * z)
            val now = System.currentTimeMillis()

            if (acceleration > 15 && now - lastShakeTime > 1000) {
                lastShakeTime = now
                logShakeMood()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    private fun logShakeMood() {
        val mood = MoodEntry(System.currentTimeMillis(), "🤯")
        moodEntries.add(0, mood)
        prefs.saveMoodEntries(moodEntries)

        Toast.makeText(requireContext(), "Mood logged: 🤯", Toast.LENGTH_SHORT).show()
    }

    private fun showEmojiPicker() {
        val emojis = listOf("😊", "😔", "😡", "😢", "😴", "😎", "🤯", "❤️")
        val emojiArray = emojis.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Select Your Mood")
            .setItems(emojiArray) { _, which ->
                val selectedEmoji = emojiArray[which]
                val entry = MoodEntry(System.currentTimeMillis(), selectedEmoji)
                moodEntries.add(0, entry)
                prefs.saveMoodEntries(moodEntries)
                adapter.notifyItemInserted(0)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager.unregisterListener(shakeListener)
    }

}