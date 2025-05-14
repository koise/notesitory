package com.example.notesitory

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder): Long

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): LiveData<List<Folder>>

    @Query("SELECT * FROM folders WHERE id = :folderId")
    fun getFolderById(folderId: Int): LiveData<Folder>

    @Query("SELECT COUNT(*) FROM notes WHERE folderId = :folderId")
    fun getNotesCountInFolder(folderId: Int): LiveData<Int>
} 