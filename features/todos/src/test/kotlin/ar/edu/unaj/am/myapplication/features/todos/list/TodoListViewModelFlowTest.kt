package ar.edu.unaj.am.myapplication.features.todos.list

import app.cash.turbine.test
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Los tests de [TodoListViewModelTest] miran una FOTO del estado (`uiState.value`).
 * Acá verificamos la PELÍCULA: qué estados emitió el ViewModel y en qué orden.
 * Eso importa porque el usuario tiene que ver un indicador de carga antes de la lista.
 *
 * ¿Por qué esta clase usa StandardTestDispatcher y no el Unconfined de la otra?
 *
 * - UnconfinedTestDispatcher ejecuta cada corrutina APENAS se lanza. Cuando el test
 *   llega a observar, el init del ViewModel ya terminó y Loading se perdió.
 * - StandardTestDispatcher encola el trabajo y lo ejecuta cuando el test lo permite
 *   (al suspenderse en awaitItem()). Así podemos ver el estado intermedio.
 *
 * Moraleja: el dispatcher de test no es un detalle de configuración, es lo que decide
 * QUÉ podés observar. Es el error más común al testear corrutinas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModelFlowTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeTodoRepository(listOf(todo(id = 1, title = "Tarea")))

    private fun createViewModel() = TodoListViewModel(
        observeTodos = ObserveTodosUseCase(repository),
        refreshTodos = RefreshTodosUseCase(repository),
        addTodo = AddTodoUseCase(repository),
        toggleTodo = ToggleTodoUseCase(repository),
        deleteTodo = DeleteTodoUseCase(repository)
    )

    @Test
    fun `uiState goes from Loading to Success`() = runTest {
        createViewModel().uiState.test {
            assertEquals(TodoListUiState.Loading, awaitItem())

            val success = awaitItem() as TodoListUiState.Success
            assertEquals(listOf(1), success.todos.map { it.id })

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState goes from Loading to Error when there is no connection`() = runTest {
        repository.refreshResult = AppResult.Error(DomainError.Network)

        createViewModel().uiState.test {
            assertEquals(TodoListUiState.Loading, awaitItem())
            assertEquals(TodoListUiState.Error(DomainError.Network), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
