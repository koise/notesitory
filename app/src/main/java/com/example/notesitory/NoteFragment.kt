package com.example.notesitory

import android.content.Intent
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

class NoteFragment : Fragment() {

    private lateinit var adapter: NoteAdapter
    private val noteList = mutableListOf<Note>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.noteRecyclerView)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Initialize adapter
        adapter = NoteAdapter(
            noteList,
            onDeleteClick = { position -> deleteNote(position) },
            onNoteClick = { selectedNote ->
                val intent = Intent(requireContext(), ViewNote::class.java).apply {
                    putExtra("title", selectedNote.title)
                    putExtra("description", selectedNote.description)
                    putExtra("date", selectedNote.date)
                }
                startActivity(intent)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // ✅ Add Initial Data
        noteList.add(Note("Mga Bagay na gusto ko magwala", "ito gagawin mo mamaya", "March 13, 2025"))
        noteList.add(Note("Mga kanta ni Lany", "mamaya mo na patugtugin pag malungkot kana https://youtu.be/qJcfMeAeZAQ", "March 12, 2025"))
        adapter.notifyDataSetChanged()

        // Swipe to delete with UX enhancements
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val holder = viewHolder as NoteAdapter.NoteViewHolder
                holder.btnDelete.visibility = View.GONE
                deleteNote(position)
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val holder = viewHolder as NoteAdapter.NoteViewHolder
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
                holder.noteFrame.translationX = dX

                // Call the superclass to handle other drawing
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    fun addNoteItem(title: String, description: String, date: String) {
        val newNote = Note(title, description, date)
        noteList.add(newNote)
        adapter.notifyItemInserted(noteList.size - 1)
    }

    fun updateNoteItem(position: Int, newTitle: String, newDescription: String, newDate: String) {
        if (position < noteList.size) {
            noteList[position] = Note(newTitle, newDescription, newDate)
            adapter.notifyItemChanged(position)
        }
    }



    // ✅ Delete item with Snackbar for undo
    private fun deleteNote(position: Int) {
        val removedItem = noteList[position]
        noteList.removeAt(position)
        adapter.notifyItemRemoved(position)

        Snackbar.make(recyclerView, "Deleted: ${removedItem.title}", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                noteList.add(position, removedItem)
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
