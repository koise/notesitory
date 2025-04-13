package com.example.notesitory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(
    private val todoList: MutableList<Todo>,
    private val onDeleteClick: (Int) -> Unit,  // ✅ Delete callback
    private val onCheckboxClick: (Int, Boolean) -> Unit  // ✅ Checkbox click callback
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val motherLayout: RelativeLayout = itemView.findViewById(R.id.motherLayout)
        val todoFrame: RelativeLayout = itemView.findViewById(R.id.todoFrame)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val titleText: TextView = itemView.findViewById(R.id.txtTitle)
        val checkboxDone: CheckBox = itemView.findViewById(R.id.checkboxDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        if (position !in todoList.indices) return

        val todo = todoList[position]
        holder.titleText.text = todo.title
        holder.checkboxDone.isChecked = todo.status

        // ✅ Apply strikethrough dynamically
        updateStrikethrough(holder.titleText, todo.status)

        // Reset swipe state
        holder.todoFrame.translationX = 0f
        holder.btnDelete.visibility = View.GONE

        // ✅ Delete Button Click
        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }

        holder.checkboxDone.setOnCheckedChangeListener { _, isChecked ->
            onCheckboxClick(position, isChecked)
        }
    }

    override fun getItemCount(): Int = todoList.size

    private fun updateStrikethrough(textView: TextView, isCompleted: Boolean) {
        textView.paintFlags = if (isCompleted) {
            textView.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
}
