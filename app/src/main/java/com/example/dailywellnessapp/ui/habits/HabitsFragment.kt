package com.example.dailywellnessapp.ui.habits

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailywellnessapp.R
import com.example.dailywellnessapp.data.SharedPrefsManager
import com.example.dailywellnessapp.model.Habit
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitsFragment : Fragment(R.layout.fragment_habits) {
    private lateinit var adapter: HabitAdapter
    private lateinit var prefs: SharedPrefsManager
    private val habits = mutableListOf<Habit>()
    private val predefinedCategories = listOf("Health", "Productivity", "Mindfulness", "Fitness", "Learning")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = SharedPrefsManager(requireContext())

        habits.clear()
        habits.addAll(prefs.loadHabits())
        updateCompletionText()

        adapter = HabitAdapter(habits) { updatedHabit ->
            prefs.saveHabits(habits)
            updateCompletionText()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.habitRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val fab = view?.findViewById<FloatingActionButton>(R.id.fabAddHabit)
                if (dy > 0) fab?.hide() else fab?.show()
            }
        })

        view.findViewById<FloatingActionButton>(R.id.fabAddHabit).setOnClickListener {
            showAddHabitDialog()
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val position = vh.adapterPosition
                val habit = habits[position]

                if (direction == ItemTouchHelper.LEFT) {
                    habits.removeAt(position)
                    prefs.saveHabits(habits)
                    adapter.notifyItemRemoved(position)
                    updateCompletionText()
                    updateEmptyMessage()
                } else {
                    showEditHabitDialog(habit, position)
                }
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = Paint()
                val iconMargin = 32
                val iconSize = 64

                val icon: Drawable?
                if (dX > 0) {
                    paint.color = Color.parseColor("#4CAF50")
                    canvas.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), itemView.left + dX, itemView.bottom.toFloat(), paint)
                    icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit)
                    icon?.setBounds(
                        itemView.left + iconMargin,
                        itemView.top + (itemView.height - iconSize) / 2,
                        itemView.left + iconMargin + iconSize,
                        itemView.bottom - (itemView.height - iconSize) / 2
                    )
                } else {
                    paint.color = Color.parseColor("#F44336")
                    canvas.drawRect(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), paint)
                    icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
                    icon?.setBounds(
                        itemView.right - iconMargin - iconSize,
                        itemView.top + (itemView.height - iconSize) / 2,
                        itemView.right - iconMargin,
                        itemView.bottom - (itemView.height - iconSize) / 2
                    )
                }

                icon?.draw(canvas)
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        updateEmptyMessage()
    }

    private fun showEditHabitDialog(habit: Habit, position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_habit, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.inputHabitName)
        val spinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val confirmButton = dialogView.findViewById<Button>(R.id.btnConfirmEdit)

        nameInput.setText(habit.name)

        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, predefinedCategories)
        spinner.adapter = adapterSpinner
        val categoryIndex = predefinedCategories.indexOf(habit.category)
        if (categoryIndex >= 0) spinner.setSelection(categoryIndex)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        confirmButton.setOnClickListener {
            val newName = nameInput.text.toString()
            val newCategory = spinner.selectedItem.toString()

            if (newName.isNotBlank()) {
                habit.name = newName
                habit.category = newCategory
                prefs.saveHabits(habits)
                adapter.notifyItemChanged(position)
                updateCompletionText()
                dialog.dismiss()
                updateEmptyMessage()
            } else {
                nameInput.error = "Please enter a habit name"
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showAddHabitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.inputHabitName)
        val spinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, predefinedCategories)
        spinner.adapter = adapterSpinner
        val confirmButton = dialogView.findViewById<Button>(R.id.btnConfirmAdd)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        confirmButton.setOnClickListener {
            val name = nameInput.text.toString()
            val category = spinner.selectedItem.toString()

            if (name.isNotBlank()) {
                val newHabit = Habit(id = habits.size + 1, name = name, category = category)
                habits.add(newHabit)
                prefs.saveHabits(habits)
                adapter.notifyItemInserted(habits.size - 1)
                updateCompletionText()
                dialog.dismiss()
                updateEmptyMessage()
            } else {
                nameInput.error = "Please enter a habit name"
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun updateCompletionText() {
        val completed = habits.count { it.isCompleted }
        val percent = if (habits.isNotEmpty()) (completed * 100 / habits.size) else 0

        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
        val percentText = view?.findViewById<TextView>(R.id.progressPercent)

        progressBar?.let {
            val animator = ObjectAnimator.ofInt(it, "progress", it.progress, percent)
            animator.duration = 600
            animator.interpolator = DecelerateInterpolator()
            animator.start()
        }

        percentText?.text = "$percent%"
    }

    private fun updateEmptyMessage() {
        val emptyMessage = view?.findViewById<TextView>(R.id.emptyMessage)
        emptyMessage?.visibility = if (habits.isEmpty()) View.VISIBLE else View.GONE
    }
}