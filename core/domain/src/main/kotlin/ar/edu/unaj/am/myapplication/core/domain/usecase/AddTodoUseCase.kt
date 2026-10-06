package ar.edu.unaj.am.myapplication.core.domain.usecase

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository

/**
 * Un caso de uso NO es solo un "pasamanos" al repositorio:
 * acá vive la validación de negocio (título obligatorio, sin espacios sobrantes).
 */
class AddTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(title: String, description: String): AppResult<Todo> {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return AppResult.Error(DomainError.EmptyTitle)
        return repository.addTodo(cleanTitle, description.trim())
    }
}
