package ar.edu.unaj.am.myapplication.features.todos.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.usecase.GetTodoUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.UpdateTodoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * El id llega por constructor: el ViewModel carga sus datos en `init`,
 * sin depender de que la UI llame a `loadTodo()` desde un LaunchedEffect.
 * Si rotamos la pantalla, el ViewModel sobrevive y NO vuelve a cargar.
 */
class TodoDetailViewModel(
    private val todoId: Int,
    private val getTodo: GetTodoUseCase,
    private val updateTodo: UpdateTodoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TodoDetailUiState>(TodoDetailUiState.Loading)
    val uiState: StateFlow<TodoDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    fun onEditClick() = updateSuccess { state ->
        state.copy(
            form = TodoForm(
                title = state.todo.title,
                description = state.todo.description,
                isCompleted = state.todo.isCompleted
            )
        )
    }

    fun onCancelEdit() = updateSuccess { it.copy(form = null) }

    fun onTitleChange(value: String) = updateForm { it.copy(title = value) }

    fun onDescriptionChange(value: String) = updateForm { it.copy(description = value) }

    fun onCompletedChange(value: Boolean) = updateForm { it.copy(isCompleted = value) }

    fun onSave() {
        val state = _uiState.value as? TodoDetailUiState.Success ?: return
        val form = state.form ?: return
        if (!form.isValid || state.isSaving) return

        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            val result = updateTodo(
                state.todo.copy(
                    title = form.title,
                    description = form.description,
                    isCompleted = form.isCompleted
                )
            )
            updateSuccess { current ->
                when (result) {
                    is AppResult.Success -> current.copy(
                        todo = result.data,
                        form = null,
                        isSaving = false,
                        isSaved = true
                    )
                    is AppResult.Error -> current.copy(isSaving = false, userMessage = result.error)
                }
            }
        }
    }

    fun onUserMessageShown() = updateSuccess { it.copy(userMessage = null) }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = TodoDetailUiState.Loading
            _uiState.value = when (val result = getTodo(todoId)) {
                is AppResult.Success -> TodoDetailUiState.Success(todo = result.data)
                is AppResult.Error -> TodoDetailUiState.Error(result.error)
            }
        }
    }

    private fun updateSuccess(transform: (TodoDetailUiState.Success) -> TodoDetailUiState.Success) {
        _uiState.update { state ->
            if (state is TodoDetailUiState.Success) transform(state) else state
        }
    }

    private fun updateForm(transform: (TodoForm) -> TodoForm) = updateSuccess { state ->
        state.form?.let { state.copy(form = transform(it)) } ?: state
    }
}
