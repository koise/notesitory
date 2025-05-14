package com.example.notesitory

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    
    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }
    
    suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note)
    }
    
    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }
    
    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }
    
    suspend fun deleteNoteById(id: Int) {
        noteDao.deleteNoteById(id)
    }
    
    fun getNotesByFolder(folderId: Int): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folderId)
    }
    
    fun getNotesWithoutFolder(): Flow<List<Note>> {
        return noteDao.getNotesWithoutFolder()
    }
    
    fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query)
    }
    
    fun getNotesByTag(tag: String): Flow<List<Note>> {
        return noteDao.getNotesByTag(tag)
    }
} 