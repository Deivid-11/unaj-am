package ar.edu.unaj.am.myapplication.core.domain.usecase

import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Regla de negocio: las tareas pendientes se muestran primero.
 * La UI no ordena nada, solo pinta lo que recibe.
 */
class ObserveTodosUseCase @Inject constructor(private val repository: TodoRepository) {
    operator fun invoke(): Flow<List<Todo>> =
        repository.observeTodos().map { todos ->
            todos.sortedWith(compareBy<Todo> { it.isCompleted }.thenBy { it.id })
        }
}
