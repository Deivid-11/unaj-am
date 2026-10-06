package ar.edu.unaj.am.myapplication.core.data.mapper

import ar.edu.unaj.am.myapplication.core.domain.model.Author
import ar.edu.unaj.am.myapplication.core.domain.model.Book
import ar.edu.unaj.am.myapplication.core.domain.model.BookPage
import ar.edu.unaj.am.myapplication.core.network.model.BookDto
import ar.edu.unaj.am.myapplication.core.network.model.BooksResponseDto
import ar.edu.unaj.am.myapplication.core.network.model.PersonDto

// El mapper no solo copia campos: adapta el formato del servidor al que necesita la app.

private const val AUTO_SUMMARY_SUFFIX = "(This is an automatically generated summary.)"

internal fun BooksResponseDto.toDomain(currentPage: Int) = BookPage(
    books = results.map { it.toDomain() },
    nextPage = if (next != null) currentPage + 1 else null
)

internal fun BookDto.toDomain() = Book(
    id = id,
    title = title,
    authors = authors.map { it.toDomain() },
    summary = summaries.firstOrNull()?.removeSuffix(AUTO_SUMMARY_SUFFIX)?.trim(),
    subjects = subjects,
    languages = languages,
    // "formats" es un mapa MIME -> URL: elegimos lo que nos sirve
    coverUrl = formats["image/jpeg"],
    readUrl = formats["text/html"] ?: formats["text/plain; charset=utf-8"],
    downloadCount = downloadCount
)

internal fun PersonDto.toDomain() = Author(
    name = name.toDisplayName(),
    birthYear = birthYear,
    deathYear = deathYear
)

/** La API manda "Austen, Jane"; en la app queremos "Jane Austen". */
internal fun String.toDisplayName(): String {
    val parts = split(", ")
    return if (parts.size == 2) "${parts[1]} ${parts[0]}" else this
}
