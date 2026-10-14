package ar.edu.unaj.am.myapplication.core.testing

import ar.edu.unaj.am.myapplication.core.domain.model.Author
import ar.edu.unaj.am.myapplication.core.domain.model.Book
import ar.edu.unaj.am.myapplication.core.domain.model.Todo

/**
 * Constructores de datos de prueba con valores por defecto.
 *
 * Un test se lee mejor cuando solo nombra lo que le importa:
 *
 *     val tarea = todo(title = "Comprar pan")   // el resto da igual
 *
 * Y si mañana Todo gana un campo nuevo, se arregla acá y no en veinte tests.
 */
fun todo(
    id: Int = 1,
    title: String = "Tarea $id",
    description: String = "",
    isCompleted: Boolean = false
) = Todo(id = id, title = title, description = description, isCompleted = isCompleted)

fun book(
    id: Int = 1,
    title: String = "Libro $id",
    authors: List<Author> = emptyList(),
    summary: String? = null,
    subjects: List<String> = emptyList(),
    languages: List<String> = listOf("es"),
    coverUrl: String? = null,
    readUrl: String? = null,
    downloadCount: Int = 0
) = Book(
    id = id,
    title = title,
    authors = authors,
    summary = summary,
    subjects = subjects,
    languages = languages,
    coverUrl = coverUrl,
    readUrl = readUrl,
    downloadCount = downloadCount
)

fun author(
    name: String = "Autor de prueba",
    birthYear: Int? = null,
    deathYear: Int? = null
) = Author(name = name, birthYear = birthYear, deathYear = deathYear)
