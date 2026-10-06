package ar.edu.unaj.am.myapplication.core.domain.model

/** Modelo de dominio de un libro. Solo lo que la app necesita, con nombres de nuestro negocio. */
data class Book(
    val id: Int,
    val title: String,
    val authors: List<Author>,
    val summary: String?,
    val subjects: List<String>,
    val languages: List<String>,
    val coverUrl: String?,
    val readUrl: String?,
    val downloadCount: Int
)

data class Author(
    val name: String,
    val birthYear: Int?,
    val deathYear: Int?
)

/** Una página de resultados. [nextPage] es null cuando no hay más para cargar. */
data class BookPage(
    val books: List<Book>,
    val nextPage: Int?
)
