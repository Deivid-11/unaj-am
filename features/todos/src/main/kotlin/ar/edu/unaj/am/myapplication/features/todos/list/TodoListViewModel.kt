package ar.edu.unaj.am.myapplication.features.todos.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.domain.usecase.AddTodoUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.DeleteTodoUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.ObserveTodosUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.RefreshTodosUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.ToggleTodoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Los casos de uso llegan por constructor (inyección de dependencias).
 * El ViewModel no sabe si detrás hay Retrofit, Room o un fake: solo conoce el dominio.
 * Eso lo hace fácil de testear (ver TodoListViewModelTest).
 */
@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val observeTodos: ObserveTodosUseCase,
    private val refreshTodos: RefreshTodosUseCase,
    private val addTodo: AddTodoUseCase,
    private val toggleTodo: ToggleTodoUseCase,
    private val deleteTodo: DeleteTodoUseCase
) : ViewModel() {

    // Privado y mutable adentro, público y de solo lectura afuera:
    // únicamente el ViewModel puede cambiar el estado.
    private val _uiState = MutableStateFlow<TodoListUiState>(TodoListUiState.Loading)
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    init {
        // Observamos la fuente de verdad: cualquier cambio (agregar, borrar, editar
        // desde el detalle…) llega solo, sin tener que volver a pedir la lista.
        viewModelScope.launch {
            observeTodos().collect { todos ->
                _uiState.update { current ->
                    if (current is TodoListUiState.Success) current.copy(todos = todos) else current
                }
            }
        }
        load()
    }

    fun onRetry() = load()

    fun onRefresh() {
        val current = _uiState.value as? TodoListUiState.Success ?: return
        _uiState.value = current.copy(isRefreshing = true)
        viewModelScope.launch {
            val result = refreshTodos()
            _uiState.update { state ->
                if (state !is TodoListUiState.Success) return@update state
                state.copy(
                    isRefreshing = false,
                    // Offline-first: si falla, seguimos mostrando lo que ya teníamos + un aviso.
                    userMessage = (result as? AppResult.Error)?.error
                )
            }
        }
    }

    fun onAddTodo(title: String, description: String) = launchAction {
        addTodo(title, description)
    }

    fun onToggleTodo(todo: Todo) = launchAction {
        toggleTodo(todo)
    }

    fun onDeleteTodo(id: Int) = launchAction {
        deleteTodo(id)
    }

    fun onUserMessageShown() {
        _uiState.update { state ->
            if (state is TodoListUiState.Success) state.copy(userMessage = null) else state
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = TodoListUiState.Loading
            _uiState.value = when (val result = refreshTodos()) {
                is AppResult.Success -> TodoListUiState.Success(todos = observeTodos().first())
                is AppResult.Error -> TodoListUiState.Error(result.error)
            }
        }
    }

    /** Ejecuta una acción y, si falla, deja el error en el estado para mostrarlo en un snackbar. */
    private fun launchAction(action: suspend () -> AppResult<*>) {
        viewModelScope.launch {
            val result = action()
            if (result is AppResult.Error) showMessage(result.error)
        }
    }

    private fun showMessage(error: DomainError) {
        _uiState.update { state ->
            if (state is TodoListUiState.Success) state.copy(userMessage = error) else state
        }
    }
}
