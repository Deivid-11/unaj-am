package ar.edu.unaj.am.myapplication.core.domain.repository

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio. El dominio define QUÉ necesita;
 * la implementación (CÓMO se obtiene) vive en :core:data.
 *
 * Esto es la "inversión de dependencias": :core:data depende de :core:domain,
 * nunca al revés.
 */
interface TodoRepository {

    /** Fuente de verdad observable: emite cada vez que cambian las tareas. */
    fun observeTodos(): Flow<List<Todo>>

    /** Pide las tareas al "servidor" y actualiza el caché local. */
    suspend fun refreshTodos(): AppResult<Unit>

    suspend fun getTodo(id: Int): AppResult<Todo>

    suspend fun addTodo(title: String, description: String): AppResult<Todo>

    suspend fun updateTodo(todo: Todo): AppResult<Todo>

    suspend fun deleteTodo(id: Int): AppResult<Unit>
}
