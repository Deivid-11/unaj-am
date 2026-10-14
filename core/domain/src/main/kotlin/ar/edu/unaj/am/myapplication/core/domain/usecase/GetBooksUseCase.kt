package ar.edu.unaj.am.myapplication.core.domain.usecase

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.BookPage
import ar.edu.unaj.am.myapplication.core.domain.repository.BookRepository
import javax.inject.Inject

class GetBooksUseCase @Inject constructor(private val repository: BookRepository) {
    suspend operator fun invoke(page: Int = FIRST_PAGE): AppResult<BookPage> = repository.getBooks(page)

    companion object {
        const val FIRST_PAGE = 1
    }
}
