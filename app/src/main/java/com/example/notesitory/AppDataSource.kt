package com.example.notesitory

import android.content.Context

class AppDataSource(context: Context) {
    val database = AppDatabase.getDatabase(context)
    val noteRepository = NoteRepository(database.noteDao())
    val folderRepository = FolderRepository(database.folderDao())
    
    companion object {
        @Volatile
        private var INSTANCE: AppDataSource? = null
        
        fun getInstance(context: Context): AppDataSource {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDataSource(context)
                INSTANCE = instance
                instance
            }
        }
    }
} 