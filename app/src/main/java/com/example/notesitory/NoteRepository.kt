package com.example.notesitory

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    // Retrieve all non-deleted notes
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    // Retrieve notes by folder (non-deleted)
    fun getNotesByFolder(folder: String): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folder)
    }

    // Insert a new note
    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    // Soft delete a note
    suspend fun softDelete(noteId: Int) {
        noteDao.softDeleteNote(noteId)
    }

    // Restore a deleted note
    suspend fun restore(noteId: Int) {
        noteDao.restoreNote(noteId)
    }
}
