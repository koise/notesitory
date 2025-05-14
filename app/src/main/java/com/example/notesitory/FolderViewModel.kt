package com.example.notesitory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FolderViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: FolderRepository
    val allFolders: LiveData<List<Folder>>
    
    init {
        val folderDao = AppDatabase.getDatabase(application).folderDao()
        repository = FolderRepository(folderDao)
        allFolders = repository.allFolders
    }
    
    fun insertFolder(name: String, description: String = "", color: String = "#FFFFFF") {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        val folder = Folder(
            name = name,
            description = description,
            creationDate = currentDate,
            color = color
        )
        
        viewModelScope.launch {
            repository.insertFolder(folder)
        }
    }
    
    fun updateFolder(folder: Folder) = viewModelScope.launch {
        repository.updateFolder(folder)
    }
    
    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        repository.deleteFolder(folder)
    }
    
    fun getFolderById(folderId: Int): LiveData<Folder> {
        return repository.getFolderById(folderId)
    }
    
    fun getNotesCountInFolder(folderId: Int): LiveData<Int> {
        return repository.getNotesCountInFolder(folderId)
    }
} 