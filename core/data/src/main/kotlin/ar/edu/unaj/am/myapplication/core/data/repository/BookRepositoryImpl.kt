package ar.edu.unaj.am.myapplication.core.data.repository

import ar.edu.unaj.am.myapplication.core.data.mapper.toAppResult
import ar.edu.unaj.am.myapplication.core.data.mapper.toDomain
import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.Book
import ar.edu.unaj.am.myapplication.core.domain.model.BookPage
import ar.edu.unaj.am.myapplication.core.domain.repository.BookRepository
import ar.edu.unaj.am.myapplication.core.network.BookApi
import ar.edu.unaj.am.myapplication.core.network.safeApiCall
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Datos desde la red (Gutendex) + un caché en memoria.
 *
 * La lista ya trae el libro completo, así que lo guardamos: al abrir el detalle
 * no hace falta volver a pedirlo (y Gutendex puede tardar mucho en /books/{id}).
 * El ViewModel no sabe nada de esto: decidir de dónde salen los datos es trabajo del repositorio.
 * Para una estrategia offline-first con base local, compará con [TodoRepositoryImpl].
 */
class BookRepositoryImpl @Inject constructor(
    private val api: BookApi
) : BookRepository {

    private val memoryCache = ConcurrentHashMap<Int, Book>()

    override suspend fun getBooks(page: Int): AppResult<BookPage> =
        safeApiCall { api.getBooks(page) }.toAppResult { response ->
            response.toDomain(currentPage = page).also { bookPage ->
                bookPage.books.forEach { memoryCache[it.id] = it }
            }
        }

    override suspend fun getBook(id: Int): AppResult<Book> {
        memoryCache[id]?.let { return AppResult.Success(it) }
        return safeApiCall { api.getBook(id) }.toAppResult { dto ->
            dto.toDomain().also { memoryCache[id] = it }
        }
    }
}
