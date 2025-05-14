package com.example.notesitory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Note::class, Folder::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migration from version 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create folders table
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS folders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "creationDate TEXT NOT NULL, " +
                    "color TEXT NOT NULL)"
                )
                
                // Add folderId column to notes table
                db.execSQL("ALTER TABLE notes ADD COLUMN folderId INTEGER")
            }
        }
        
        // Migration from version 2 to 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ensure folders table exists
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS folders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "creationDate TEXT NOT NULL, " +
                    "color TEXT NOT NULL)"
                )
                
                // Create new notes table with foreign key
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS notes_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "folderId INTEGER, " +
                    "FOREIGN KEY (folderId) REFERENCES folders(id) ON DELETE CASCADE)"
                )
                
                // Copy data from old table to new table (safely)
                db.execSQL(
                    "INSERT INTO notes_new (id, title, description, date, folderId) " +
                    "SELECT id, title, description, date, folderId FROM notes"
                )
                
                // Remove old table and rename new table
                db.execSQL("DROP TABLE notes")
                db.execSQL("ALTER TABLE notes_new RENAME TO notes")
                
                // Create index on folderId
                db.execSQL("CREATE INDEX index_notes_folderId ON notes(folderId)")
            }
        }

        // Migration from version 3 to 4 - adding tags to notes
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add tags column to notes table
                db.execSQL("ALTER TABLE notes ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
            }
        }
        
        // Migration from version 4 to 5 - removing todos
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop todos table as we're removing this functionality
                db.execSQL("DROP TABLE IF EXISTS todos")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notesitory_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 