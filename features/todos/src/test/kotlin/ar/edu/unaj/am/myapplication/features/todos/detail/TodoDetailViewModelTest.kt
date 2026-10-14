package ar.edu.unaj.am.myapplication.features.todos.detail

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.usecase.GetTodoUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.UpdateTodoUseCase
import ar.edu.unaj.am.myapplication.core.testing.FakeTodoRepository
import ar.edu.unaj.am.myapplication.core.testing.MainDispatcherRule
import ar.edu.unaj.am.myapplication.core.testing.todo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Este ViewModel usa @AssistedInject (el id lo pone la navegación, no el grafo).
 * En el test eso no cambia NADA: se construye con su constructor normal.
 *
 * Es la prueba de que Hilt no se mete en el medio: inyectar dependencias es, al final,
 * pasar parámetros por constructor.
 */
class TodoDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTodoRepository(
        listOf(todo(id = 1, title = "Estudiar Hilt", description = "Capítulo 2"))
    )

    private fun createViewModel(todoId: Int = 1) = TodoDetailViewModel(
        todoId = todoId,
        getTodo = GetTodoUseCase(repository),
        updateTodo = UpdateTodoUseCase(repository)
    )

    @Test
    fun `loads the todo by id`() = runTest {
        val state = createViewModel().uiState.value as TodoDetailUiState.Success

        assertEquals("Estudiar Hilt", state.todo.title)
        assertFalse(state.isEditing)
    }

    @Test
    fun `unknown id emits NotFound`() = runTest {
        val viewModel = createViewModel(todoId = 404)

        assertEquals(TodoDetailUiState.Error(DomainError.NotFound), viewModel.uiState.value)
    }

    @Test
    fun `editing opens a form with the current values`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEditClick()

        val form = (viewModel.uiState.value as TodoDetailUiState.Success).form
        assertEquals("Estudiar Hilt", form?.title)
    }

    @Test
    fun `blank title does not save`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEditClick()

        viewModel.onTitleChange("   ")
        viewModel.onSave()

        val state = viewModel.uiState.value as TodoDetailUiState.Success
        assertTrue(state.isEditing) // sigue en modo edición: no guardó
        assertEquals("Estudiar Hilt", repository.todos.value.single().title)
    }

    @Test
    fun `saving updates the todo and closes the form`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEditClick()

        viewModel.onTitleChange("Estudiar testing")
        viewModel.onSave()

        val state = viewModel.uiState.value as TodoDetailUiState.Success
        assertEquals("Estudiar testing", state.todo.title)
        assertNull(state.form)
        assertTrue(state.isSaved)
        assertEquals("Estudiar testing", repository.todos.value.single().title)
    }

    @Test
    fun `save error keeps the form and reports the message`() = runTest {
        val repositoryConError = object : FailingTodoRepository(repository) {}
        val viewModel = TodoDetailViewModel(
            todoId = 1,
            getTodo = GetTodoUseCase(repositoryConError),
            updateTodo = UpdateTodoUseCase(repositoryConError)
        )
        viewModel.onEditClick()
        viewModel.onTitleChange("Otro título")

        viewModel.onSave()

        val state = viewModel.uiState.value as TodoDetailUiState.Success
        assertEquals(DomainError.Network, state.userMessage)
        assertFalse(state.isSaving)
        assertTrue(state.isEditing) // no se pierde lo que el usuario escribió
    }
}

/** Decora el fake compartido para que solo falle al guardar. */
private open class FailingTodoRepository(
    private val delegate: FakeTodoRepository
) : ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository by delegate {
    override suspend fun updateTodo(
        todo: ar.edu.unaj.am.myapplication.core.domain.model.Todo
    ): AppResult<ar.edu.unaj.am.myapplication.core.domain.model.Todo> =
        AppResult.Error(DomainError.Network)
}
