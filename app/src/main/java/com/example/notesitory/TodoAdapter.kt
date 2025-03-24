package com.example.notesitory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(
    private val todoList: MutableList<Todo>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val motherLayout: RelativeLayout = itemView.findViewById(R.id.motherLayout)
        val todoFrame: RelativeLayout = itemView.findViewById(R.id.todoFrame)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val titleText: TextView = itemView.findViewById(R.id.txtTitle)
        val descText: TextView = itemView.findViewById(R.id.txtDescription)
        val dateText: TextView = itemView.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todoList[position]

        holder.titleText.text = todo.title
        holder.descText.text = todo.description
        holder.dateText.text = todo.date

        // Reset swipe state
        holder.todoFrame.translationX = 0f
        holder.btnDelete.visibility = View.GONE

        // Delete button click
        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = todoList.size
}
