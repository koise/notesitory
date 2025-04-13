package com.example.notesitory

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import java.util.Date
import java.util.Locale

class NewNote : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_note)

        // Initialize ViewModel
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        // Get reference to views
        titleEditText = findViewById(R.id.titleEditText)
        notesEditText = findViewById(R.id.notesEditText)

        // Handle window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.newnote)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set back arrow functionality (to save and navigate back)
        val arrow = findViewById<View>(R.id.arrow)
        arrow.setOnClickListener {
            saveNote()  // Save the note before navigating back
            finish()    // Finish the activity to navigate back
        }
    }

    // Function to save the note (using ViewModel)
    private fun saveNote() {
        val title = titleEditText.text.toString()
        val noteText = notesEditText.text.toString()

        if (title.isNotEmpty() || noteText.isNotEmpty()) {
            var date = getCurrentDate()
            val note = Note(title = title, description = noteText, folder = "Notes",  date = date)
            noteViewModel.insert(note)
            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Note cannot be empty!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        // Implement logic to get the current date (could be in a specific format)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
