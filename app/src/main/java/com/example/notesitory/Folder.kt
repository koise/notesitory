package com.example.notesitory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val creationDate: String,
    val color: String = "#FFFFFF"
) 