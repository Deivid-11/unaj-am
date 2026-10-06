package ar.edu.unaj.am.myapplication.features.books.list

import ar.edu.unaj.am.myapplication.core.domain.model.Book
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError

sealed interface BookListUiState {

    data object Loading : BookListUiState

    data class Success(
        val books: List<Book>,
        val canLoadMore: Boolean,
        val isLoadingMore: Boolean = false,
        // Error al traer la página siguiente: no tapamos la lista, mostramos "Reintentar" al final.
        val loadMoreError: DomainError? = null
    ) : BookListUiState

    data class Error(val error: DomainError) : BookListUiState
}
