package ar.edu.unaj.am.myapplication.features.books.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.usecase.GetBookUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookDetailViewModel(
    private val bookId: Int,
    private val getBook: GetBookUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookDetailUiState>(BookDetailUiState.Loading)
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = BookDetailUiState.Loading
            _uiState.value = when (val result = getBook(bookId)) {
                is AppResult.Success -> BookDetailUiState.Success(result.data)
                is AppResult.Error -> BookDetailUiState.Error(result.error)
            }
        }
    }
}
