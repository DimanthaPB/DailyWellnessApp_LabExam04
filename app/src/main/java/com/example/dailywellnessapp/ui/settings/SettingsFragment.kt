package com.example.dailywellnessapp.ui.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.dailywellnessapp.R
import com.example.dailywellnessapp.data.SharedPrefsManager
import com.example.dailywellnessapp.model.MoodEntry
import com.example.dailywellnessapp.utils.HydrationReceiver
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.io.File
import java.io.FileOutputStream

private val emojiMap = mapOf(
    "😊" to 8, "😎" to 7, "❤️" to 6, "😴" to 5,
    "😔" to 4, "😢" to 3, "😡" to 2, "🤯" to 1
)

class EmojiAxisFormatter : ValueFormatter() {
    private val emojiMap = mapOf(
        1f to "🤯", 2f to "😡", 3f to "😢", 4f to "😔",
        5f to "😴", 6f to "❤️", 7f to "😎", 8f to "😊"
    )

    override fun getFormattedValue(value: Float): String {
        return emojiMap[value] ?: ""
    }
}

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var prefs: SharedPrefsManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = SharedPrefsManager(requireContext())

        val switch = view.findViewById<Switch>(R.id.switchEnableReminder)
        val spinner = view.findViewById<Spinner>(R.id.intervalSpinner)
        val button = view.findViewById<Button>(R.id.btnSetReminder)

        val intervals = listOf(1, 2, 3, 4, 6)
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, intervals)

        val savedInterval = prefs.getHydrationInterval()
        val index = intervals.indexOf(savedInterval)
        if (index >= 0) spinner.setSelection(index)

        switch.isChecked = prefs.isHydrationReminderEnabled()

        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.setHydrationReminderEnabled(isChecked)
            if (isChecked) {
                val selected = spinner.selectedItem as Int
                scheduleHydrationReminder(selected)
                Toast.makeText(requireContext(), "💧 Reminder set every $selected hours!", Toast.LENGTH_SHORT).show()
            } else {
                cancelHydrationReminder()
                Toast.makeText(requireContext(), "Hydration reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }

        button.setOnClickListener {
            val selected = spinner.selectedItem as Int
            prefs.saveHydrationInterval(selected)

            if (switch.isChecked) {
                scheduleHydrationReminder(selected)
                Toast.makeText(requireContext(), "💧 Reminder set every $selected hours!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Reminder saved but not active", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.btnExportChart).setOnClickListener {
            val chart = view.findViewById<LineChart>(R.id.moodChart)
            exportChartAsImage(chart)
        }
        setupMoodChart()
    }

    private fun exportChartAsImage(chart: LineChart) {
        val bitmap = chart.chartBitmap
        val filename = "mood_chart_${System.currentTimeMillis()}.png"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DailyWellnessApp")
        }

        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Toast.makeText(requireContext(), "Chart saved to Pictures/DailyWellnessApp", Toast.LENGTH_LONG).show()
        } ?: run {
            Toast.makeText(requireContext(), "Failed to save chart", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMoodChart() {
        val chart = view?.findViewById<LineChart>(R.id.moodChart) ?: return
        val moodEntries = prefs.loadMoodEntries()

        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000L

        val entries = moodEntries
            .filter { it.timestamp in sevenDaysAgo..now }
            .sortedBy { it.timestamp }
            .mapIndexed { index, mood ->
                val score = emojiMap[mood.emoji] ?: 0
                Entry(index.toFloat(), score.toFloat())
            }

        val dataSet = LineDataSet(entries, "Mood Trend").apply {
            color = Color.parseColor("#FFAB91")
            valueTextColor = Color.parseColor("#8D6E63")
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.parseColor("#FFAB91"))
            setDrawFilled(true)
            fillColor = Color.parseColor("#FFE0B2")
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        chart.data = LineData(dataSet)
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.axisLeft.valueFormatter = EmojiAxisFormatter()
        chart.axisLeft.granularity = 1f
        chart.axisLeft.setLabelCount(emojiMap.size, true)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.animateY(800)
        chart.invalidate()
    }

    private fun cancelHydrationReminder() {
        val intent = Intent(requireContext(), HydrationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 1, intent, PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
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
