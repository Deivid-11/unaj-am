package ar.edu.unaj.am.myapplication

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository
import ar.edu.unaj.am.myapplication.core.domain.usecase.AddTodoUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.GetBooksUseCase
import ar.edu.unaj.am.myapplication.core.domain.usecase.ObserveTodosUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * Test instrumentado (corre en un emulador o dispositivo):
 *
 *     ./gradlew :app:connectedDebugAndroidTest
 *
 * Verifica el GRAFO completo: que Hilt pueda construir todo lo que la app necesita y
 * que los scopes estén bien. Un unit test no puede comprobar esto, porque en un unit
 * test las dependencias las arma el propio test a mano.
 *
 * Si falta un @Binds, sobra un @Singleton o hay una dependencia circular, falla acá
 * en vez de fallar en la cara del usuario al abrir la pantalla.
 */
@HiltAndroidTest
class DependencyGraphTest {

    // Arma el componente de test (con FakeNetworkModule en lugar de NetworkModule).
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var getBooks: GetBooksUseCase
    @Inject lateinit var addTodo: AddTodoUseCase
    @Inject lateinit var observeTodos: ObserveTodosUseCase

    // Dos pedidos del MISMO tipo: si el binding es @Singleton, tiene que ser el mismo objeto.
    @Inject lateinit var todoRepository: TodoRepository
    @Inject lateinit var todoRepositoryOtraVez: TodoRepository

    @Before
    fun setUp() {
        // Recién acá se completan los @Inject de arriba.
        hiltRule.inject()
    }

    @Test
    fun hiltProvidesTheWholeChainUpToTheUseCases() = runTest {
        // getBooks -> BookRepository -> BookApi: tres capas que Hilt resolvió solo,
        // y la de red es la falsa que instalamos en FakeNetworkModule.
        val result = getBooks(page = GetBooksUseCase.FIRST_PAGE)

        val page = (result as AppResult.Success).data
        assertEquals(listOf("Martín Fierro", "Rayuela"), page.books.map { it.title })
    }

    @Test
    fun todoRepositoryIsASingleton() {
        // Este era el motivo del `by lazy` en el viejo AppContainer: si cada pantalla
        // recibiera su propio repositorio, cada una tendría su propia lista de tareas.
        assertSame(todoRepository, todoRepositoryOtraVez)
    }

    @Test
    fun writingThroughOneUseCaseIsVisibleFromAnother() = runTest {
        // Dos casos de uso distintos comparten el repositorio: lo que escribe uno,
        // lo observa el otro. Si el scope estuviera mal, esto fallaría.
        addTodo("Tarea de prueba", "")

        val titles = observeTodos().first().map { it.title }
        assertEquals(1, titles.count { it == "Tarea de prueba" })
    }
}
