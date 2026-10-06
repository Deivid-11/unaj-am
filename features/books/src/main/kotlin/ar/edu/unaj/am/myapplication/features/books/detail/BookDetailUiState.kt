package ar.edu.unaj.am.myapplication.features.books.detail

import ar.edu.unaj.am.myapplication.core.domain.model.Book
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError

sealed interface BookDetailUiState {
    data object Loading : BookDetailUiState
    data class Success(val book: Book) : BookDetailUiState
    data class Error(val error: DomainError) : BookDetailUiState
}
