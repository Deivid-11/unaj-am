package ar.edu.unaj.am.myapplication.core.domain.usecase

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository
import javax.inject.Inject

class RefreshTodosUseCase @Inject constructor(private val repository: TodoRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.refreshTodos()
}
