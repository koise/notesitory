package com.example.notesitory

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import android.widget.ArrayAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout

class NewNote : AppCompatActivity() {
    private val noteViewModel: NoteViewModel by viewModels()
    private lateinit var folderViewModel: FolderViewModel
    private lateinit var titleEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var folderSelector: TextView
    private lateinit var tagInput: EditText
    private lateinit var chipGroup: ChipGroup
    private lateinit var addTagButton: ImageView
    private var selectedFolderId: Int? = null
    private val tags = mutableSetOf<String>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_note)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.newnote)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        folderViewModel = ViewModelProvider(this)[FolderViewModel::class.java]
        
        titleEditText = findViewById(R.id.titleEditText)
        notesEditText = findViewById(R.id.notesEditText)
        folderSelector = findViewById(R.id.folder_selector)
        tagInput = findViewById(R.id.tagInput)
        chipGroup = findViewById(R.id.chipGroupTags)
        addTagButton = findViewById(R.id.addTagButton)
        val backButton = findViewById<ImageView>(R.id.arrow)
        
        // Set up folder selector
        folderSelector.setOnClickListener {
            showFolderSelectionDialog()
        }
        
        // Set up tag functionality
        setupTagInput()
        
        // Set up back button to save and exit
        backButton.setOnClickListener {
            saveNote()
            finish()
        }
        
        // Handle back press with the OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                saveNote()
                finish()
            }
        })
    }
    
    private fun setupTagInput() {
        addTagButton.setOnClickListener {
            val tagText = tagInput.text.toString().trim()
            if (tagText.isNotEmpty()) {
                addTag(tagText)
                tagInput.text.clear()
            }
        }
    }
    
    private fun addTag(tagText: String) {
        if (tags.add(tagText)) { // Only add if it's not already in the set
            val chip = Chip(this).apply {
                text = tagText
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    chipGroup.removeView(this)
                    tags.remove(tagText)
                }
            }
            chipGroup.addView(chip)
        }
    }
    
    private fun showFolderSelectionDialog() {
        folderViewModel.allFolders.observe(this) { folders ->
            if (folders.isNotEmpty()) {
                // Create list with "No folder" option at the top
                val folderNames = mutableListOf("No folder")
                folderNames.addAll(folders.map { it.name })
                
                // Create map of folder names to ids
                val folderMap = mutableMapOf<String, Int?>()
                folderMap["No folder"] = null
                folders.forEach { folderMap[it.name] = it.id }
                
                AlertDialog.Builder(this)
                    .setTitle("Select Folder")
                    .setItems(folderNames.toTypedArray()) { _, which ->
                        val selectedFolderName = folderNames[which]
                        selectedFolderId = folderMap[selectedFolderName]
                        
                        // Update the UI to show the selected folder
                        folderSelector.text = selectedFolderName
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(this, "No folders available. Create folders first.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun saveNote() {
        val title = titleEditText.text.toString().trim()
        val description = notesEditText.text.toString().trim()
        val tagString = tags.joinToString(",")
        
        if (title.isNotEmpty() || description.isNotEmpty()) {
            // Use empty title if none provided
            val finalTitle = if (title.isEmpty()) "Untitled" else title
            noteViewModel.insertNote(finalTitle, description, selectedFolderId, tagString)
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
        }
    }
}