package ar.edu.unaj.am.myapplication.core.network

import ar.edu.unaj.am.myapplication.core.network.model.BookDto
import ar.edu.unaj.am.myapplication.core.network.model.BooksResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** API real de Gutendex. Retrofit genera la implementación a partir de las anotaciones. */
interface BookApi {

    @GET("books/")
    suspend fun getBooks(@Query("page") page: Int): BooksResponseDto

    @GET("books/{id}/")
    suspend fun getBook(@Path("id") id: Int): BookDto
}
