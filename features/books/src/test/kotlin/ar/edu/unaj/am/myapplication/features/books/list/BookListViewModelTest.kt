package ar.edu.unaj.am.myapplication.features.books.list

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.Book
import ar.edu.unaj.am.myapplication.core.domain.model.BookPage
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.repository.BookRepository
import ar.edu.unaj.am.myapplication.core.domain.usecase.GetBooksUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookListViewModelTest {

    private val repository = FakeBookRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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

/** Dos páginas de un libro cada una. */
private class FakeBookRepository : BookRepository {

    val requestedPages = mutableListOf<Int>()
    var error: DomainError? = null

    override suspend fun getBooks(page: Int): AppResult<BookPage> {
        error?.let { return AppResult.Error(it) }
        requestedPages += page
        return AppResult.Success(
            BookPage(books = listOf(book(page)), nextPage = if (page < 2) page + 1 else null)
        )
    }

    override suspend fun getBook(id: Int): AppResult<Book> = AppResult.Success(book(id))

    private fun book(id: Int) = Book(
        id = id,
        title = "Libro $id",
        authors = emptyList(),
        summary = null,
        subjects = emptyList(),
        languages = emptyList(),
        coverUrl = null,
        readUrl = null,
        downloadCount = 0
    )
}
