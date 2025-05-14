package com.example.notesitory

import androidx.lifecycle.LiveData

class FolderRepository(private val folderDao: FolderDao) {
    
    val allFolders: LiveData<List<Folder>> = folderDao.getAllFolders()
    
    suspend fun insertFolder(folder: Folder): Long {
        return folderDao.insertFolder(folder)
    }
    
    suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(folder)
    }
    
    suspend fun deleteFolder(folder: Folder) {
        folderDao.deleteFolder(folder)
    }
    
    fun getFolderById(folderId: Int): LiveData<Folder> {
        return folderDao.getFolderById(folderId)
    }
    
    fun getNotesCountInFolder(folderId: Int): LiveData<Int> {
        return folderDao.getNotesCountInFolder(folderId)
    }
} 