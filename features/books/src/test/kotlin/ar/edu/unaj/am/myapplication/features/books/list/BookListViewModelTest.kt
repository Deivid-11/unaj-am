package ar.edu.unaj.am.myapplication.features.books.list

import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.usecase.GetBooksUseCase
import ar.edu.unaj.am.myapplication.core.testing.FakeBookRepository
import ar.edu.unaj.am.myapplication.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class BookListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Dos páginas de un libro cada una.
    private val repository = FakeBookRepository(pageCount = 2)

    private fun createViewModel() = BookListViewModel(GetBooksUseCase(repository))

    @Test
    fun `load more appends next page`() = runTest {
        val viewModel = createViewModel()

        viewModel.onLoadMore()

        val state = viewModel.uiState.value as BookListUiState.Success
        assertEquals(listOf(1, 2), state.books.map { it.id })
        assertEquals(listOf(1, 2), repository.requestedPages)
    }

    @Test
    fun `last page disables load more`() = runTest {
        val viewModel = createViewModel()
        viewModel.onLoadMore() // página 2 es la última

        viewModel.onLoadMore()

        val state = viewModel.uiState.value as BookListUiState.Success
        assertFalse(state.canLoadMore)
        assertEquals(listOf(1, 2), repository.requestedPages)
    }

    @Test
    fun `load more error keeps loaded books`() = runTest {
        val viewModel = createViewModel()
        repository.error = DomainError.Network

        viewModel.onLoadMore()

        val state = viewModel.uiState.value as BookListUiState.Success
        assertEquals(listOf(1), state.books.map { it.id })
        assertEquals(DomainError.Network, state.loadMoreError)
    }
}
