package ar.edu.unaj.am.myapplication.core.domain.usecase

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.Book
import ar.edu.unaj.am.myapplication.core.domain.repository.BookRepository
import javax.inject.Inject

class GetBookUseCase @Inject constructor(private val repository: BookRepository) {
    suspend operator fun invoke(id: Int): AppResult<Book> = repository.getBook(id)
}
