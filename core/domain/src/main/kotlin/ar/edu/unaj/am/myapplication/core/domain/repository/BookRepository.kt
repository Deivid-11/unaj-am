package ar.edu.unaj.am.myapplication.core.domain.repository

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.Book
import ar.edu.unaj.am.myapplication.core.domain.model.BookPage

/**
 * Mismo patrón que [TodoRepository], distinta estrategia: acá no hay caché local,
 * los datos vienen siempre de la red (network-only). Las features no notan la diferencia.
 */
interface BookRepository {
    suspend fun getBooks(page: Int): AppResult<BookPage>
    suspend fun getBook(id: Int): AppResult<Book>
}
