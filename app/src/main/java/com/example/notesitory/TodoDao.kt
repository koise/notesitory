package com.example.notesitory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Insert
    suspend fun insert(todo: Todo)

    @Query("SELECT * FROM todos WHERE deleted = 0")  // Get only non-deleted todos
    fun getAllTodos(): Flow<List<Todo>>

    @Query("UPDATE todos SET deleted = 1 WHERE id = :todoId")  // Soft delete a todo
    suspend fun softDeleteTodo(todoId: Int)
}
