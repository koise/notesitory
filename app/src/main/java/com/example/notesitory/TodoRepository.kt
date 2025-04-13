package com.example.notesitory

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val allTodos: Flow<List<Todo>> = todoDao.getAllTodos()
    suspend fun insert(todo: Todo) {
        todoDao.insert(todo)
    }
    suspend fun softDelete(todoId: Int) {
        todoDao.softDeleteTodo(todoId)
    }
}
