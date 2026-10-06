package ar.edu.unaj.am.myapplication.features.todos.list

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository
import ar.edu.unaj.am.myapplication.core.domain.usecase.AddTodoUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.DeleteTodoUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.ObserveTodosUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.RefreshTodosUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.ToggleTodoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModelTest {

    private val repository = FakeTodoRepository()

    @Before
    fun setUp() {
        // viewModelScope usa Dispatchers.Main, que no existe en la JVM: lo reemplazamos.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = TodoListViewModel(
        observeTodos = ObserveTodosUseCase(repository),
        refreshTodos = RefreshTodosUseCase(repository),
        addTodo = AddTodoUseCase(repository),
        toggleTodo = ToggleTodoUseCase(repository),
        deleteTodo = DeleteTodoUseCase(repository)
    )

    @Test
    fun `initial load emits Success with todos`() = runTest {
        repository.todos.value = listOf(Todo(1, "Tarea", "", isCompleted = false))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value as TodoListUiState.Success
        assertEquals(1, state.todos.size)
    }

    @Test
    fun `initial load emits Error when refresh fails`() = runTest {
        repository.refreshResult = AppResult.Error(DomainError.Network)

        val viewModel = createViewModel()

        assertEquals(TodoListUiState.Error(DomainError.Network), viewModel.uiState.value)
    }

    @Test
    fun `added todo shows up without reloading`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAddTodo("Nueva", "")

        val state = viewModel.uiState.value as TodoListUiState.Success
        assertEquals(listOf("Nueva"), state.todos.map { it.title })
    }

    @Test
    fun `failed refresh keeps list and shows message`() = runTest {
        repository.todos.value = listOf(Todo(1, "Tarea", "", isCompleted = false))
        val viewModel = createViewModel()
        repository.refreshResult = AppResult.Error(DomainError.Network)

        viewModel.onRefresh()

        val state = viewModel.uiState.value as TodoListUiState.Success
        assertEquals(1, state.todos.size)
        assertEquals(DomainError.Network, state.userMessage)
    }
}

private class FakeTodoRepository : TodoRepository {

    val todos = MutableStateFlow<List<Todo>>(emptyList())
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
