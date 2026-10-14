package ar.edu.unaj.am.myapplication.core.testing

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.Book
import ar.edu.unaj.am.myapplication.core.domain.model.BookPage
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.repository.BookRepository

/**
 * Doble de prueba del catálogo: [pageCount] páginas de un libro cada una.
 *
 * Registra en [requestedPages] qué páginas se pidieron, para poder verificar que la
 * paginación no pide dos veces lo mismo.
 */
class FakeBookRepository(
    private val pageCount: Int = 2
) : BookRepository {

    val requestedPages = mutableListOf<Int>()

    /** Si no es null, la próxima llamada falla con este error. */
    var error: DomainError? = null

    override suspend fun getBooks(page: Int): AppResult<BookPage> {
        error?.let { return AppResult.Error(it) }
        requestedPages += page
        return AppResult.Success(
            BookPage(
                books = listOf(book(id = page)),
                nextPage = if (page < pageCount) page + 1 else null
            )
        )
    }

    override suspend fun getBook(id: Int): AppResult<Book> {
        error?.let { return AppResult.Error(it) }
        return AppResult.Success(book(id = id))
    }
}
