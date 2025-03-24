package com.example.notesitory

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class TodoFragment : Fragment() {

    private lateinit var adapter: TodoAdapter
    private val todoList = mutableListOf<Todo>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.todoRecyclerView)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Initialize adapter
        adapter = TodoAdapter(todoList) { position -> deleteTodo(position) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // ✅ Add Initial Data
        todoList.add(Todo("Kinakamunhkahian ko", "gagawin ito kinabukasan", "March 20, 2025"))
        todoList.add(Todo("TODO List kanina", "Maghugas ng kamay hanggang magdugo ang utak", "March 20, 2025"))
        adapter.notifyDataSetChanged()

        // Swipe to delete with UX enhancements
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val holder = viewHolder as TodoAdapter.TodoViewHolder
                holder.btnDelete.visibility = View.GONE
                deleteTodo(position)
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val holder = viewHolder as TodoAdapter.TodoViewHolder
                val deleteThreshold = -150f

                if (dX < 0) {
                    // Calculate the swipe progress as a value between 0 and 1
                    val swipeProgress = Math.min(1f, Math.abs(dX) / recyclerView.width)

                    // Retrieve Holo Red colors
                    val holoRedLight = ContextCompat.getColor(requireContext(), R.color.holo_red_light)
                    val holoRedDark = ContextCompat.getColor(requireContext(), R.color.holo_red_dark)

                    // Interpolate the color based on swipe progress
                    val color = blendColors(holoRedLight, holoRedDark, swipeProgress)

                    // Set the background color of the item based on the swipe progress
                    holder.motherLayout.setBackgroundColor(color)

                    // Show the delete button when the swipe exceeds the threshold
                    if (Math.abs(dX) > deleteThreshold) {
                        if (holder.btnDelete.visibility == View.GONE) {
                            holder.btnDelete.visibility = View.VISIBLE
                            holder.btnDelete.alpha = 0f
                            holder.btnDelete.animate().alpha(1f).setDuration(100).start()
                        }
                    } else {
                        holder.btnDelete.visibility = View.GONE
                    }
                } else {
                    // Reset visibility when swipe is released
                    holder.btnDelete.visibility = View.GONE
                }

                // Translate the item based on the swipe distance
                holder.todoFrame.translationX = dX

                // Call the superclass to handle other drawing
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    fun addTodoItem(title: String, description: String, date: String) {
        val newTodo = Todo(title, description, date)
        todoList.add(newTodo)
        adapter.notifyItemInserted(todoList.size - 1)
    }

    fun updateTodoItem(position: Int, newTitle: String, newDescription: String, newDate: String) {
        if (position < todoList.size) {
            todoList[position] = Todo(newTitle, newDescription, newDate)
            adapter.notifyItemChanged(position)
        }
    }



    // ✅ Delete item with Snackbar for undo
    private fun deleteTodo(position: Int) {
        val removedItem = todoList[position]
        todoList.removeAt(position)
        adapter.notifyItemRemoved(position)

        Snackbar.make(recyclerView, "Deleted: ${removedItem.title}", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                todoList.add(position, removedItem)
                adapter.notifyItemInserted(position)
            }
            .setActionTextColor(Color.WHITE)
            .show()
    }

    private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val a = (Color.alpha(color1) * inverseRatio + Color.alpha(color2) * ratio).toInt()
        val r = (Color.red(color1) * inverseRatio + Color.red(color2) * ratio).toInt()
        val g = (Color.green(color1) * inverseRatio + Color.green(color2) * ratio).toInt()
        val b = (Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio).toInt()
        return Color.argb(a, r, g, b)
    }



}
