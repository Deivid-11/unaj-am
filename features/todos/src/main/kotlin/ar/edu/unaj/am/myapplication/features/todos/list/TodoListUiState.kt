package ar.edu.unaj.am.myapplication.features.todos.list

import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.model.Todo

/**
 * TODO lo que la pantalla necesita para dibujarse, en un solo objeto inmutable.
 *
 * Sealed interface: la pantalla está en UNO de estos estados a la vez, y el `when`
 * de la UI es exhaustivo (si agregamos un estado nuevo y no lo manejamos, no compila).
 */
sealed interface TodoListUiState {

    data object Loading : TodoListUiState

    data class Success(
        val todos: List<Todo>,
        val isRefreshing: Boolean = false,
        // Mensaje de un solo uso (snackbar). La UI avisa cuando lo mostró con onUserMessageShown().
        val userMessage: DomainError? = null
    ) : TodoListUiState

    data class Error(val error: DomainError) : TodoListUiState
}
