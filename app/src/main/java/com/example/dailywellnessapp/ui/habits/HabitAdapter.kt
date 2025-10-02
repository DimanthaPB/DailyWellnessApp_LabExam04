package com.example.dailywellnessapp.ui.habits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dailywellnessapp.R
import com.example.dailywellnessapp.model.Habit

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onToggle: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.habitCheckBox)
        val categoryLabel: TextView = itemView.findViewById(R.id.categoryLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.text = habit.name
        holder.checkBox.isChecked = habit.isCompleted
        holder.categoryLabel.text = "Category: ${habit.category}"

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                val currentHabit = habits[holder.adapterPosition]
                currentHabit.isCompleted = isChecked
                onToggle(currentHabit)
            }
        }
    }

    override fun getItemCount() = habits.size
}
