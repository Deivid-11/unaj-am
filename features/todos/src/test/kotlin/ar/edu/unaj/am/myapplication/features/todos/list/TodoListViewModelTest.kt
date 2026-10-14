package ar.edu.unaj.am.myapplication.features.todos.list

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.usecase.AddTodoUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.DeleteTodoUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.ObserveTodosUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.RefreshTodosUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.ToggleTodoUseCase
import ar.edu.unaj.am.myapplication.core.testing.FakeTodoRepository
import ar.edu.unaj.am.myapplication.core.testing.MainDispatcherRule
import ar.edu.unaj.am.myapplication.core.testing.todo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Tests del ViewModel SIN Android: no hay emulador, ni red, ni base de datos.
 * Es posible porque el ViewModel depende de interfaces del dominio, no de implementaciones.
 *
 * Fijate que los casos de uso se construyen a mano: la inyección de dependencias es para
 * la app, no para los tests. Un unit test arma exactamente el escenario que quiere probar.
 */
class TodoListViewModelTest {

    // Reemplaza Dispatchers.Main (que en la JVM no existe) por uno de test.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTodoRepository()

    private fun createViewModel() = TodoListViewModel(
        observeTodos = ObserveTodosUseCase(repository),
        refreshTodos = RefreshTodosUseCase(repository),
        addTodo = AddTodoUseCase(repository),
        toggleTodo = ToggleTodoUseCase(repository),
        deleteTodo = DeleteTodoUseCase(repository)
    )

    @Test
    fun `initial load emits Success with todos`() = runTest {
        repository.todos.value = listOf(todo(id = 1, title = "Tarea"))

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
        repository.todos.value = listOf(todo(id = 1, title = "Tarea"))
        val viewModel = createViewModel()
        repository.refreshResult = AppResult.Error(DomainError.Network)

        viewModel.onRefresh()

        val state = viewModel.uiState.value as TodoListUiState.Success
        assertEquals(1, state.todos.size)
        assertEquals(DomainError.Network, state.userMessage)
    }
}
