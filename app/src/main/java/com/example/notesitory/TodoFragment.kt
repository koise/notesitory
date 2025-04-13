package com.example.notesitory

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class TodoFragment : Fragment() {

    private lateinit var adapter: TodoAdapter
    private val todoList: MutableList<Todo> = mutableListOf()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.CompletedRecyclerView)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = TodoAdapter(todoList, { position -> deleteTodo(position) }, { position, isChecked ->
            if (position in todoList.indices) {
                todoList[position].status = isChecked
                adapter.notifyItemChanged(position)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // ✅ Add Initial Data
        todoList.add(Todo(1, "Kinakamunhkahian ko", false, false))
        todoList.add(Todo(2, "TODO List kanina", false, false))
        todoList.add(Todo(3, "Talagang talaga", false, false))
        todoList.add(Todo(4, "Kain punas ta", false, false))
        adapter.notifyDataSetChanged()

        // Swipe to delete with animation
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
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
                    holder.motherLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.holo_red_light))
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
                    holder.btnDelete.visibility = View.GONE
                }

                holder.todoFrame.translationX = dX
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    // ✅ Add new task
    fun addTodoItem(title: String) {
        val newTodo = Todo(todoList.size + 1, title, false, false)
        todoList.add(newTodo)
        adapter.notifyItemInserted(todoList.size - 1)
    }

    // ✅ Delete item with Snackbar for undo
    private fun deleteTodo(position: Int) {
        if (position in todoList.indices) {
            val removedItem = todoList.removeAt(position)
            adapter.notifyItemRemoved(position)

            Snackbar.make(recyclerView, "Deleted: ${removedItem.title}", Snackbar.LENGTH_LONG)
                .setAction("UNDO") {
                    todoList.add(position, removedItem)
                    adapter.notifyItemInserted(position)
                }
                .setActionTextColor(Color.WHITE)
                .show()
        }
    }

    private fun updateStrikethrough(textView: TextView, isCompleted: Boolean) {
        textView.paintFlags = if (isCompleted) {
            textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
}

