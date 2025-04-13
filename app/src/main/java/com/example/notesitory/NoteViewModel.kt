package com.example.notesitory
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    // Expose all non-deleted notes
    val allNotes = repository.allNotes

    // Get notes by folder
    fun getNotesByFolder(folder: String) = repository.getNotesByFolder(folder)

    // Add a new note
    fun addNote(title: String, description: String, folder: String, date: String) {
        viewModelScope.launch {
            repository.insert(Note(title = title, description = description, folder = folder, deleted = false, date = date))
        }
    }

    // Soft delete a note
    fun softDelete(noteId: Int) {
        viewModelScope.launch {
            repository.softDelete(noteId)
        }
    }

    // Restore a deleted note
    fun restore(noteId: Int) {
        viewModelScope.launch {
            repository.restore(noteId)
        }
    }
}
