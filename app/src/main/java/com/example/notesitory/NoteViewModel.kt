package com.example.notesitory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>
    
    // Search functionality
    private val searchQuery = MutableStateFlow("")
    val searchResults: LiveData<List<Note>>

    init {
        val dataSource = AppDataSource.getInstance(application)
        repository = dataSource.noteRepository
        allNotes = repository.allNotes.asLiveData()
        
        searchResults = searchQuery.flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allNotes
            } else {
                repository.searchNotes(query)
            }
        }.asLiveData()
    }

    fun insertNote(title: String, description: String, folderId: Int? = null, tags: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val note = Note(title = title, description = description, date = date, folderId = folderId, tags = tags)
            repository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    fun getNoteById(id: Int, callback: (Note?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = repository.getNoteById(id)
            callback(note)
        }
    }
    
    fun getNotesByFolder(folderId: Int): LiveData<List<Note>> {
        return repository.getNotesByFolder(folderId).asLiveData()
    }
    
    fun getNotesWithoutFolder(): LiveData<List<Note>> {
        return repository.getNotesWithoutFolder().asLiveData()
    }
    
    fun moveNoteToFolder(note: Note, folderId: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedNote = note.copy(folderId = folderId)
            repository.updateNote(updatedNote)
        }
    }
    
    fun search(query: String) {
        searchQuery.value = query
    }
    
    fun getNotesByTag(tag: String): LiveData<List<Note>> {
        return repository.getNotesByTag(tag).asLiveData()
    }
    
    fun addTagToNote(note: Note, newTag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTags = note.tags.split(",").filter { it.isNotBlank() }.toMutableSet()
            currentTags.add(newTag.trim())
            val updatedTags = currentTags.joinToString(",")
            val updatedNote = note.copy(tags = updatedTags)
            repository.updateNote(updatedNote)
        }
    }
    
    fun removeTagFromNote(note: Note, tagToRemove: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTags = note.tags.split(",").filter { it.isNotBlank() && it.trim() != tagToRemove.trim() }
            val updatedTags = currentTags.joinToString(",")
            val updatedNote = note.copy(tags = updatedTags)
            repository.updateNote(updatedNote)
        }
    }
} 