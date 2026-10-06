package ar.edu.unaj.am.myapplication.core.domain.usecase

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.Book
import ar.edu.unaj.am.myapplication.core.domain.repository.BookRepository

class GetBookUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(id: Int): AppResult<Book> = repository.getBook(id)
}
