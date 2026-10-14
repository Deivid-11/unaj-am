package ar.edu.unaj.am.myapplication.core.data.repository

import ar.edu.unaj.am.myapplication.core.data.mapper.toAppResult
import ar.edu.unaj.am.myapplication.core.data.mapper.toDomain
import ar.edu.unaj.am.myapplication.core.data.mapper.toDto
import ar.edu.unaj.am.myapplication.core.data.mapper.toEntity
import ar.edu.unaj.am.myapplication.core.database.dao.TodoDao
import ar.edu.unaj.am.myapplication.core.database.mapper.toDomain
import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository
import ar.edu.unaj.am.myapplication.core.network.TodoApi
import ar.edu.unaj.am.myapplication.core.network.safeApiCall
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Une la red y la base local con la estrategia "offline-first":
 *
 * - La base local es la ÚNICA fuente de verdad: la UI siempre observa [TodoDao].
 * - La red solo se usa para traer datos nuevos o confirmar cambios; cuando responde bien,
 *   escribimos en la base y el Flow notifica a quien esté observando.
 */
class TodoRepositoryImpl @Inject constructor(
    private val api: TodoApi,
    private val dao: TodoDao
) : TodoRepository {

    override fun observeTodos(): Flow<List<Todo>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshTodos(): AppResult<Unit> =
        safeApiCall { api.getTodos() }.toAppResult { dtos ->
            dao.replaceAll(dtos.map { it.toEntity() })
        }

    override suspend fun getTodo(id: Int): AppResult<Todo> =
        safeApiCall { api.getTodo(id) }.toAppResult { dto ->
            dao.upsert(dto.toEntity())
            dto.toDomain()
        }

    override suspend fun addTodo(title: String, description: String): AppResult<Todo> =
        safeApiCall { api.createTodo(title, description) }.toAppResult { dto ->
            dao.upsert(dto.toEntity())
            dto.toDomain()
        }

    override suspend fun updateTodo(todo: Todo): AppResult<Todo> =
        safeApiCall { api.updateTodo(todo.toDto()) }.toAppResult { dto ->
            dao.upsert(dto.toEntity())
            dto.toDomain()
        }

    override suspend fun deleteTodo(id: Int): AppResult<Unit> =
        safeApiCall { api.deleteTodo(id) }.toAppResult {
            dao.deleteById(id)
        }
}
