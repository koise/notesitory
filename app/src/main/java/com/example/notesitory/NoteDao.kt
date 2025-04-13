package com.example.notesitory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // Insert a new note into the database
    @Insert
    suspend fun insert(note: Note)

    // Get all non-deleted notes
    @Query("SELECT * FROM notes WHERE deleted = 0")
    fun getAllNotes(): Flow<List<Note>>

    // Get notes by folder
    @Query("SELECT * FROM notes WHERE folder = :folder AND deleted = 0")
    fun getNotesByFolder(folder: String): Flow<List<Note>>

    // Soft delete a note (mark it as deleted)
    @Query("UPDATE notes SET deleted = 1 WHERE id = :noteId")
    suspend fun softDeleteNote(noteId: Int)

    // Restore a deleted note
    @Query("UPDATE notes SET deleted = 0 WHERE id = :noteId")
    suspend fun restoreNote(noteId: Int)
}
