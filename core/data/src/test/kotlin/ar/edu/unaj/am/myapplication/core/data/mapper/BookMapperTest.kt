package ar.edu.unaj.am.myapplication.core.data.mapper

import ar.edu.unaj.am.myapplication.core.network.model.BookDto
import ar.edu.unaj.am.myapplication.core.network.model.BooksResponseDto
import ar.edu.unaj.am.myapplication.core.network.model.PersonDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookMapperTest {

    private val dto = BookDto(
        id = 1342,
        title = "Pride and Prejudice",
        authors = listOf(PersonDto("Austen, Jane", 1775, 1817)),
        summaries = listOf("A novel. (This is an automatically generated summary.)"),
        formats = mapOf(
            "image/jpeg" to "https://example.com/cover.jpg",
            "text/html" to "https://example.com/book.html"
        ),
        downloadCount = 10
    )

    @Test
    fun `author name is reordered`() {
        assertEquals("Jane Austen", dto.toDomain().authors.single().name)
    }

    @Test
    fun `author name with more than one comma is kept as is`() {
        assertEquals("Tolstoy, Leo, graf", "Tolstoy, Leo, graf".toDisplayName())
    }

    @Test
    fun `auto generated suffix is removed from summary`() {
        assertEquals("A novel.", dto.toDomain().summary)
    }

    @Test
    fun `cover and read url are picked from formats`() {
        val book = dto.toDomain()
        assertEquals("https://example.com/cover.jpg", book.coverUrl)
        assertEquals("https://example.com/book.html", book.readUrl)
    }

    @Test
    fun `next page is null on last page`() {
        val response = BooksResponseDto(count = 1, next = null, results = listOf(dto))
        assertNull(response.toDomain(currentPage = 3).nextPage)
    }
}
