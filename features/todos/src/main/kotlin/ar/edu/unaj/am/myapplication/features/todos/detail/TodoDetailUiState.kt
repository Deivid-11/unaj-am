package ar.edu.unaj.am.myapplication.features.todos.detail

import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.model.Todo

sealed interface TodoDetailUiState {

    data object Loading : TodoDetailUiState

    data class Success(
        val todo: Todo,
        // null = modo lectura. Así es imposible estar "editando" sin datos de formulario.
        val form: TodoForm? = null,
        val isSaving: Boolean = false,
        // Señal de navegación basada en estado: cuando es true, la UI vuelve atrás.
        val isSaved: Boolean = false,
        val userMessage: DomainError? = null
    ) : TodoDetailUiState {
        val isEditing: Boolean get() = form != null
    }

    data class Error(val error: DomainError) : TodoDetailUiState
}

/** Valores que el usuario está escribiendo, separados de la tarea guardada ([TodoDetailUiState.Success.todo]). */
data class TodoForm(
    val title: String,
    val description: String,
    val isCompleted: Boolean
) {
    val isValid: Boolean get() = title.isNotBlank()
}
