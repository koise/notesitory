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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import android.widget.TextView

class NoteFragment : Fragment() {

    private lateinit var adapter: NoteAdapter
    private val noteList = mutableListOf<Note>()
    private val filteredNoteList = mutableListOf<Note>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var notesHeader: TextView
    private val viewModel: NoteViewModel by viewModels()
    
    // Folder related variables
    private var folderId: Int? = null
    private var folderName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.noteRecyclerView)
        notesHeader = view.findViewById(R.id.notesHeader)
        
        // Get folder information from arguments
        arguments?.let {
            folderId = it.getInt("folderId", -1)
            if (folderId == -1) folderId = null
            folderName = it.getString("folderName")
        }
        
        // Set the header text based on folder
        if (!folderName.isNullOrEmpty()) {
            notesHeader.text = folderName
        } else {
            notesHeader.text = "All"
        }
        
        setupRecyclerView()
        observeNotes()
    }
    
    private fun observeNotes() {
        if (folderId != null) {
            // Observe notes from a specific folder
            viewModel.getNotesByFolder(folderId!!).observe(viewLifecycleOwner) { notes ->
                updateNoteList(notes)
            }
        } else {
            // Observe all notes
            viewModel.allNotes.observe(viewLifecycleOwner) { notes ->
                updateNoteList(notes)
            }
        }
    }
    
    private fun updateNoteList(notes: List<Note>) {
        noteList.clear()
        noteList.addAll(notes)
        
        // Reset filtered list
        filteredNoteList.clear()
        filteredNoteList.addAll(noteList)
        
        // Update UI
        adapter.notifyDataSetChanged()
        updateEmptyState()
    }
    
    private fun updateEmptyState() {
        val emptyStateView = view?.findViewById<View>(R.id.emptyStateView)
        val contentView = view?.findViewById<View>(R.id.contentLayout)
        
        if (filteredNoteList.isEmpty()) {
            emptyStateView?.visibility = View.VISIBLE
            contentView?.visibility = View.GONE
        } else {
            emptyStateView?.visibility = View.GONE
            contentView?.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        // Initialize adapter with filtered list
        adapter = NoteAdapter(
            filteredNoteList,
            onDeleteClick = { position -> deleteNote(position) },
            onNoteClick = { selectedNote ->
                val intent = Intent(requireContext(), ViewNote::class.java).apply {
                    putExtra("id", selectedNote.id)
                    putExtra("title", selectedNote.title)
                    putExtra("description", selectedNote.description)
                    putExtra("tags", selectedNote.tags)
                }
                startActivity(intent)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        setupSwipeToDelete()
    }
    
    private fun setupSwipeToDelete() {
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
                    val swipeProgress = Math.min(1f, Math.abs(dX) / recyclerView.width * 2) // Double the rate for more responsive color change

                    // Get error color from resources
                    val errorColor = ContextCompat.getColor(requireContext(), R.color.error)
                    
                    // For background transition
                    val surfaceColor = ContextCompat.getColor(requireContext(), R.color.surface)
                    
                    // Interpolate the color based on swipe progress
                    val color = blendColors(surfaceColor, errorColor, swipeProgress)

                    // Set the background color of the whole item
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
                    holder.motherLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface))
                }

                // Translate the item based on the swipe distance
                holder.noteFrame.translationX = dX

                // Call the superclass to handle other drawing
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            // Reset the item appearance when swipe is canceled
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                
                val holder = viewHolder as NoteAdapter.NoteViewHolder
                holder.motherLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface))
                holder.noteFrame.translationX = 0f
                holder.btnDelete.visibility = View.GONE
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    // Delete item with Snackbar for undo
    private fun deleteNote(position: Int) {
        if (position >= 0 && position < filteredNoteList.size) {
            val removedItem = filteredNoteList[position]
            viewModel.deleteNote(removedItem)

            Snackbar.make(recyclerView, "Deleted: ${removedItem.title}", Snackbar.LENGTH_LONG)
                .setAction("UNDO") {
                    viewModel.updateNote(removedItem)
                }
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.secondary))
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.surface))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                .show()
        }
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
