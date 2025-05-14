package com.example.notesitory

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
    
    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY date DESC")
    fun getNotesByFolder(folderId: Int): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE folderId IS NULL ORDER BY date DESC")
    fun getNotesWithoutFolder(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchNotes(query: String): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE tags LIKE '%' || :tag || '%' ORDER BY date DESC")
    fun getNotesByTag(tag: String): Flow<List<Note>>
} 