package ar.edu.unaj.am.myapplication.core.domain.usecase

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository

class ToggleTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(todo: Todo): AppResult<Todo> =
        repository.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
}
