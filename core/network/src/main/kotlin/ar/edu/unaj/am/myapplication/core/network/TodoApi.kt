package ar.edu.unaj.am.myapplication.core.network

import ar.edu.unaj.am.myapplication.core.network.model.TodoDto

/**
 * Contrato de la API remota. Con Retrofit sería:
 *
 *     @GET("todos") suspend fun getTodos(): List<TodoDto>
 *
 * Hoy no nos conectamos a ningún servidor: usamos [ar.edu.unaj.am.myapplication.core.network.fake.FakeTodoApi].
 */
interface TodoApi {
    suspend fun getTodos(): List<TodoDto>
    suspend fun getTodo(id: Int): TodoDto
    suspend fun createTodo(title: String, description: String): TodoDto
    suspend fun updateTodo(todo: TodoDto): TodoDto
    suspend fun deleteTodo(id: Int)
}
