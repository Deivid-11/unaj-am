package ar.edu.unaj.am.myapplication.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Modelos EXACTOS de la API de Gutendex (https://gutendex.com/books/).
// @SerialName traduce snake_case del JSON a camelCase de Kotlin.
// Los valores por defecto nos protegen si el servidor omite un campo.

@Serializable
data class BooksResponseDto(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<BookDto> = emptyList()
)

@Serializable
data class BookDto(
    val id: Int,
    val title: String,
    val authors: List<PersonDto> = emptyList(),
    val summaries: List<String> = emptyList(),
    val subjects: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    // Mapa "tipo MIME" -> URL, por ejemplo "image/jpeg" -> portada
    val formats: Map<String, String> = emptyMap(),
    @SerialName("download_count") val downloadCount: Int = 0
)

@Serializable
data class PersonDto(
    val name: String,
    @SerialName("birth_year") val birthYear: Int? = null,
    @SerialName("death_year") val deathYear: Int? = null
)
