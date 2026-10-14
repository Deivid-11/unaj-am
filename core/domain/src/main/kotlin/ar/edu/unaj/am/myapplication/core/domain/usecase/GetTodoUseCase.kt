package ar.edu.unaj.am.myapplication.core.domain.usecase

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository
import javax.inject.Inject

class GetTodoUseCase @Inject constructor(private val repository: TodoRepository) {
    suspend operator fun invoke(id: Int): AppResult<Todo> = repository.getTodo(id)
}
