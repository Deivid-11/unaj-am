package ar.edu.unaj.am.myapplication.core.domain.usecase

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Gracias a que TodoRepository es una interfaz, testear el dominio no requiere red ni base de datos. */
class FakeTodoRepository(initial: List<Todo> = emptyList()) : TodoRepository {

    val todos = MutableStateFlow(initial)

    override fun observeTodos(): Flow<List<Todo>> = todos

    override suspend fun refreshTodos(): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun getTodo(id: Int): AppResult<Todo> =
        todos.value.find { it.id == id }?.let { AppResult.Success(it) }
            ?: AppResult.Error(DomainError.NotFound)

    override suspend fun addTodo(title: String, description: String): AppResult<Todo> {
        val todo = Todo(todos.value.size + 1, title, description, isCompleted = false)
        todos.update { it + todo }
        return AppResult.Success(todo)
    }

    override suspend fun updateTodo(todo: Todo): AppResult<Todo> {
        todos.update { list -> list.map { if (it.id == todo.id) todo else it } }
        return AppResult.Success(todo)
    }

    override suspend fun deleteTodo(id: Int): AppResult<Unit> {
        todos.update { list -> list.filterNot { it.id == id } }
        return AppResult.Success(Unit)
    }
}
