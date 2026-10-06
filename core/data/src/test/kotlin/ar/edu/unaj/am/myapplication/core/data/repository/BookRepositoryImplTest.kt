package ar.edu.unaj.am.myapplication.core.data.repository

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.network.BookApi
import ar.edu.unaj.am.myapplication.core.network.model.BookDto
import ar.edu.unaj.am.myapplication.core.network.model.BooksResponseDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookRepositoryImplTest {

    private val api = object : BookApi {
        var detailCalls = 0

        override suspend fun getBooks(page: Int) =
            BooksResponseDto(count = 1, results = listOf(BookDto(id = 1, title = "Libro 1")))

        override suspend fun getBook(id: Int): BookDto {
            detailCalls++
            return BookDto(id = id, title = "Libro $id")
        }
    }

    private val repository = BookRepositoryImpl(api)

    @Test
    fun `book already loaded in list is served from memory`() = runTest {
        repository.getBooks(page = 1)

        val result = repository.getBook(1)

        assertTrue(result is AppResult.Success)
        assertEquals(0, api.detailCalls)
    }

    @Test
    fun `unknown book is fetched from network`() = runTest {
        repository.getBook(42)

        assertEquals(1, api.detailCalls)
    }
}
