package com.example.dailywellnessapp.ui.mood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dailywellnessapp.R
import com.example.dailywellnessapp.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodAdapter(private val moods: List<MoodEntry>) :
    RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiView: TextView = itemView.findViewById(R.id.emojiView)
        val dateView: TextView = itemView.findViewById(R.id.dateView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]
        holder.emojiView.text = mood.emoji
        holder.dateView.text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            .format(Date(mood.timestamp))
    }

    override fun getItemCount() = moods.size
}