package ar.edu.unaj.am.myapplication.di

import ar.edu.unaj.am.myapplication.core.network.BookApi
import ar.edu.unaj.am.myapplication.core.network.TodoApi
import ar.edu.unaj.am.myapplication.core.network.di.NetworkModule
import ar.edu.unaj.am.myapplication.core.network.fake.FakeTodoApi
import ar.edu.unaj.am.myapplication.core.network.model.BookDto
import ar.edu.unaj.am.myapplication.core.network.model.BooksResponseDto
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * ACÁ SE VE PARA QUÉ SIRVE LA INYECCIÓN DE DEPENDENCIAS.
 *
 * @TestInstallIn le dice a Hilt: "durante los tests, en lugar de NetworkModule usá este".
 * El test corre contra la app REAL —mismos repositorios, mismos casos de uso, mismos
 * ViewModels— pero sin tocar internet: ni resultados que cambian, ni tests que fallan
 * porque Gutendex está lento.
 *
 * Nada de producción se modifica para lograrlo. Eso es imposible cuando cada clase
 * construye sus propias dependencias con `new`.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
object FakeNetworkModule {

    @Provides
    @Singleton
    fun provideBookApi(): BookApi = FakeBookApi()

    @Provides
    @Singleton
    fun provideTodoApi(): TodoApi = FakeTodoApi(latencyMs = 0)
}

/** Catálogo fijo de dos libros: los tests pueden afirmar resultados exactos. */
class FakeBookApi : BookApi {

    override suspend fun getBooks(page: Int): BooksResponseDto = BooksResponseDto(
        count = 2,
        next = null,
        results = listOf(bookDto(id = 1, title = "Martín Fierro"), bookDto(id = 2, title = "Rayuela"))
    )

    override suspend fun getBook(id: Int): BookDto = bookDto(id = id, title = "Libro $id")

    private fun bookDto(id: Int, title: String) = BookDto(
        id = id,
        title = title,
        languages = listOf("es")
    )
}
