package ar.edu.unaj.am.myapplication.core.domain.usecase

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.testing.FakeTodoRepository
import ar.edu.unaj.am.myapplication.core.testing.todo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoUseCasesTest {

    @Test
    fun `add rejects blank title`() = runTest {
        val repository = FakeTodoRepository()

        val result = AddTodoUseCase(repository)("   ", "desc")

        assertEquals(AppResult.Error(DomainError.EmptyTitle), result)
        assertTrue(repository.todos.value.isEmpty())
    }

    @Test
    fun `add trims title and description`() = runTest {
        val repository = FakeTodoRepository()

        AddTodoUseCase(repository)("  Estudiar  ", " Compose ")

        val saved = repository.todos.value.single()
        assertEquals("Estudiar", saved.title)
        assertEquals("Compose", saved.description)
    }

    @Test
    fun `observe returns pending todos first`() = runTest {
        val repository = FakeTodoRepository(
            listOf(
                todo(id = 1, title = "Hecha", isCompleted = true),
                todo(id = 2, title = "Pendiente")
            )
        )

        val todos = ObserveTodosUseCase(repository)().first()

        assertEquals(listOf(2, 1), todos.map { it.id })
    }

    @Test
    fun `toggle flips completion`() = runTest {
        val todo = todo(id = 1)
        val repository = FakeTodoRepository(listOf(todo))

        ToggleTodoUseCase(repository)(todo)

        assertTrue(repository.todos.value.single().isCompleted)
    }
}
