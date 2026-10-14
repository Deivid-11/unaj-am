package ar.edu.unaj.am.myapplication.core.domain.usecase

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository
import javax.inject.Inject

class UpdateTodoUseCase @Inject constructor(private val repository: TodoRepository) {
    suspend operator fun invoke(todo: Todo): AppResult<Todo> {
        val cleanTitle = todo.title.trim()
        if (cleanTitle.isEmpty()) return AppResult.Error(DomainError.EmptyTitle)
        return repository.updateTodo(
            todo.copy(title = cleanTitle, description = todo.description.trim())
        )
    }
}
