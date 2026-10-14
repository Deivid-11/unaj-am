package ar.edu.unaj.am.myapplication.core.testing

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Doble de prueba del repositorio de tareas, compartido por todos los módulos.
 *
 * Gracias a que TodoRepository es una INTERFAZ (dominio) y no una clase concreta,
 * testear no requiere red ni base de datos: ese es el beneficio de la inversión de
 * dependencias, el mismo que hace posible la inyección.
 *
 * Un fake escrito a mano, y no un mock generado, porque se comporta como el de verdad:
 * si agregás una tarea, la lista la refleja. Los tests quedan legibles y no se rompen
 * cada vez que cambia el orden de las llamadas.
 */
class FakeTodoRepository(initial: List<Todo> = emptyList()) : TodoRepository {

    val todos = MutableStateFlow(initial)

    /** Qué debe devolver el próximo refreshTodos(). Sirve para simular un error de red. */
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)

    override fun observeTodos(): Flow<List<Todo>> = todos

    override suspend fun refreshTodos(): AppResult<Unit> = refreshResult

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
