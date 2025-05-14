package com.example.notesitory

import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ViewNote : AppCompatActivity() {
    private val viewModel: NoteViewModel by viewModels()
    private var noteId: Int = 0
    private var currentNote: Note? = null
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var tagsChipGroup: ChipGroup
    private lateinit var addTagLayout: View
    private lateinit var tagInput: EditText
    private lateinit var addTagButton: ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_note)
        
        titleTextView = findViewById(R.id.viewTitle)
        descriptionTextView = findViewById(R.id.viewDescription)
        tagsChipGroup = findViewById(R.id.tagsChipGroup)
        addTagLayout = findViewById(R.id.addTagLayout)
        tagInput = findViewById(R.id.tagInput)
        addTagButton = findViewById(R.id.addTagButton)
        
        // Get note ID from intent
        noteId = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "No Title"
        val description = intent.getStringExtra("description") ?: "No Description"
        val tags = intent.getStringExtra("tags") ?: ""
        
        // Display note
        titleTextView.text = title
        descriptionTextView.apply {
            text = description
            Linkify.addLinks(this, Linkify.WEB_URLS)
            movementMethod = LinkMovementMethod.getInstance()
        }
        
        // Display tags
        displayTags(tags)
        
        // Setup tag addition
        setupTagInput()
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Load note from database for possible editing
        if (noteId > 0) {
            viewModel.getNoteById(noteId) { note ->
                currentNote = note
                note?.let {
                    displayTags(it.tags)
                }
            }
        }
    }
    
    private fun setupTagInput() {
        addTagButton.setOnClickListener {
            val tagText = tagInput.text.toString().trim()
            if (tagText.isNotEmpty()) {
                currentNote?.let { note ->
                    viewModel.addTagToNote(note, tagText)
                    // Refresh note data
                    viewModel.getNoteById(noteId) { updatedNote ->
                        currentNote = updatedNote
                        updatedNote?.let {
                            displayTags(it.tags)
                        }
                    }
                    tagInput.text.clear()
                }
            }
        }
    }
    
    private fun displayTags(tagString: String) {
        tagsChipGroup.removeAllViews()
        
        val tags = tagString.split(",").filter { it.isNotBlank() }
        if (tags.isEmpty()) {
            findViewById<View>(R.id.tagsContainer).visibility = View.GONE
        } else {
            findViewById<View>(R.id.tagsContainer).visibility = View.VISIBLE
            
            for (tag in tags) {
                val chip = Chip(this).apply {
                    text = tag
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        currentNote?.let { note ->
                            viewModel.removeTagFromNote(note, tag)
                            tagsChipGroup.removeView(this)
                            // Refresh note data
                            viewModel.getNoteById(noteId) { updatedNote ->
                                currentNote = updatedNote
                            }
                        }
                    }
                }
                tagsChipGroup.addView(chip)
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_view_note, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_edit -> {
                showEditDialog()
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_note, null)
        val titleEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextTitle)
        val descriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextDescription)
        
        // Pre-fill with current values
        titleEditText.setText(titleTextView.text)
        descriptionEditText.setText(descriptionTextView.text)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleEditText.text.toString()
                val description = descriptionEditText.text.toString()
                
                if (title.isNotEmpty() && description.isNotEmpty()) {
                    currentNote?.let {
                        val updatedNote = it.copy(title = title, description = description)
                        viewModel.updateNote(updatedNote)
                        
                        // Update UI
                        titleTextView.text = title
                        descriptionTextView.text = description
                        showSuccessMessage("Note updated")
                    }
                } else {
                    showErrorMessage("Please fill all fields")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                currentNote?.let {
                    viewModel.deleteNote(it)
                    showSuccessMessage("Note deleted")
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showSuccessMessage(message: String) {
        Snackbar.make(titleTextView, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.success))
            .setTextColor(getColor(R.color.white))
            .show()
    }
    
    private fun showErrorMessage(message: String) {
        Snackbar.make(titleTextView, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.error))
            .setTextColor(getColor(R.color.white))
            .show()
    }
}
