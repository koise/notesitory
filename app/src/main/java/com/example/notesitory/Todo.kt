package com.example.notesitory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    var status: Boolean,    // Track whether the todo is completed
    var deleted: Boolean = false  // Soft delete flag
)
