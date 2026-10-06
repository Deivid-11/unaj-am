package ar.edu.unaj.am.myapplication.features.books.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.usecase.GetBooksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Paginación manual: pedimos de a una página y la vamos sumando a la lista.
 * (En proyectos grandes esto lo resuelve Paging 3, pero la idea de fondo es esta).
 */
class BookListViewModel(
    private val getBooks: GetBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookListUiState>(BookListUiState.Loading)
    val uiState: StateFlow<BookListUiState> = _uiState.asStateFlow()

    // Detalle de implementación de la paginación: la UI no necesita saberlo.
    private var nextPage: Int? = GetBooksUseCase.FIRST_PAGE

    init {
        loadFirstPage()
    }

    fun onRetry() = loadFirstPage()

    fun onLoadMore() {
        val state = _uiState.value as? BookListUiState.Success ?: return
        val page = nextPage ?: return
        // Guard: si ya estamos cargando, no disparamos otra request igual.
        if (state.isLoadingMore) return

        _uiState.value = state.copy(isLoadingMore = true, loadMoreError = null)
        viewModelScope.launch {
            val result = getBooks(page)
            _uiState.update { current ->
                if (current !is BookListUiState.Success) return@update current
                when (result) {
                    is AppResult.Success -> {
                        nextPage = result.data.nextPage
                        current.copy(
                            books = current.books + result.data.books,
                            canLoadMore = nextPage != null,
                            isLoadingMore = false
                        )
                    }
                    is AppResult.Error -> current.copy(isLoadingMore = false, loadMoreError = result.error)
                }
            }
        }
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            _uiState.value = BookListUiState.Loading
            _uiState.value = when (val result = getBooks(GetBooksUseCase.FIRST_PAGE)) {
                is AppResult.Success -> {
                    nextPage = result.data.nextPage
                    BookListUiState.Success(books = result.data.books, canLoadMore = nextPage != null)
                }
                is AppResult.Error -> BookListUiState.Error(result.error)
            }
        }
    }
}
