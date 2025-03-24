package com.example.notesitory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class NoteAdapter(
    private val noteList: MutableList<Note>,
    private val onDeleteClick: (Int) -> Unit,
    private val onNoteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val motherLayout: RelativeLayout = itemView.findViewById(R.id.motherLayout)
        val noteFrame: RelativeLayout = itemView.findViewById(R.id.noteFrame)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val titleText: TextView = itemView.findViewById(R.id.txtTitle)
        val descText: TextView = itemView.findViewById(R.id.txtDescription)
        val dateText: TextView = itemView.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]

        holder.titleText.text = note.title
        holder.descText.text = note.description
        holder.dateText.text = note.date

        // Reset swipe state
        holder.noteFrame.translationX = 0f
        holder.btnDelete.visibility = View.GONE

        // Delete button click
        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }


        holder.itemView.setOnClickListener {
            onNoteClick(note)
        }
    }

    override fun getItemCount(): Int = noteList.size
}
