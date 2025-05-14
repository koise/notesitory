package com.example.notesitory

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class NoteAdapter(
    private val noteList: MutableList<Note>,
    private val onDeleteClick: (Int) -> Unit,
    private val onNoteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val motherLayout: RelativeLayout = itemView.findViewById(R.id.motherLayout)
        val noteFrame: RelativeLayout = itemView.findViewById(R.id.noteFrame)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
        val titleText: TextView = itemView.findViewById(R.id.txtTitle)
        val descText: TextView = itemView.findViewById(R.id.txtDescription)
        val dateText: TextView = itemView.findViewById(R.id.txtDate)
        val folderText: TextView = itemView.findViewById(R.id.txtFolder)
        val tagsChipGroup: ChipGroup = itemView.findViewById(R.id.noteTagsChipGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        val context = holder.itemView.context

        // Set basic note info
        holder.titleText.text = note.title
        holder.descText.text = note.description
        
        // Format and set the date
        val dateText = formatDate(note.date)
        holder.dateText.text = dateText

        // Handle folder display
        if (note.folderId != null) {
            // In a real app, you would fetch the folder name from a repository
            // For now, set it to visible with a default value
            holder.folderText.visibility = View.VISIBLE
            
            // TODO: Replace this with actual folder name lookup
            // folderRepository.getFolderName(note.folderId) would be ideal
            holder.folderText.text = "Folder"
        } else {
            holder.folderText.visibility = View.GONE
        }
        
        // Handle tags
        holder.tagsChipGroup.removeAllViews()
        if (note.tags.isNotBlank()) {
            holder.tagsChipGroup.visibility = View.VISIBLE
            val tags = note.tags.split(",").filter { it.isNotBlank() }
            
            // Limit to max 3 tags in the preview
            val displayTags = tags.take(3)
            
            displayTags.forEach { tag ->
                val chip = Chip(context).apply {
                    text = tag.trim()
                    isClickable = false
                    isCheckable = false
                    
                    // Improved styling for tags
                    chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary_variant))
                    setTextColor(ContextCompat.getColor(context, R.color.secondary))
                    textSize = 12f
                    
                    chipMinHeight = 28f
                    chipStartPadding = 8f
                    chipEndPadding = 8f
                    
                    setEnsureMinTouchTargetSize(false)
                }
                holder.tagsChipGroup.addView(chip)
            }
            
            // Add a "+X more" chip if there are additional tags
            if (tags.size > 3) {
                val moreChip = Chip(context).apply {
                    text = "+${tags.size - 3}"
                    isClickable = false
                    isCheckable = false
                    
                    // Styling for "more" chip
                    chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.ripple))
                    setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                    textSize = 12f
                    
                    chipMinHeight = 28f
                    chipStartPadding = 8f
                    chipEndPadding = 8f
                    
                    setEnsureMinTouchTargetSize(false)
                }
                holder.tagsChipGroup.addView(moreChip)
            }
        } else {
            holder.tagsChipGroup.visibility = View.GONE
        }

        // Reset swipe state
        holder.noteFrame.translationX = 0f
        holder.btnDelete.visibility = View.GONE

        // Delete button click
        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }

        // Note click
        holder.itemView.setOnClickListener {
            onNoteClick(note)
        }
    }
    
    private fun formatDate(dateString: String): String {
        // Handle different date formats
        return try {
            // Example format: "yyyy-MM-dd HH:mm:ss" to "MMM d"
            val dateParts = dateString.split(" ")[0].split("-")
            val year = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val day = dateParts[2].toInt()
            
            val monthAbbr = when(month) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> "???"
            }
            
            "$monthAbbr $day"
        } catch (e: Exception) {
            dateString // Return original if parsing fails
        }
    }

    override fun getItemCount(): Int = noteList.size
}
