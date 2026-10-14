package ar.edu.unaj.am.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ar.edu.unaj.am.myapplication.features.books.detail.BookDetailRoute
import ar.edu.unaj.am.myapplication.features.books.detail.BookDetailViewModel
import ar.edu.unaj.am.myapplication.features.books.list.BookListRoute
import ar.edu.unaj.am.myapplication.features.books.list.BookListViewModel
import ar.edu.unaj.am.myapplication.features.playground.PlaygroundScreen
import ar.edu.unaj.am.myapplication.features.todos.detail.TodoDetailRoute
import ar.edu.unaj.am.myapplication.features.todos.detail.TodoDetailViewModel
import ar.edu.unaj.am.myapplication.features.todos.list.TodoListRoute
import ar.edu.unaj.am.myapplication.features.todos.list.TodoListViewModel

/**
 * La navegación vive en :app. Las features no se conocen entre sí:
 * solo exponen callbacks (onTodoClick, onBack…) y acá decidimos qué hace cada uno.
 */
@Composable
fun AppNavDisplay(
    navigationState: NavigationState,
    modifier: Modifier = Modifier
) {
    // entryProvider: "para esta key, mostrá esta pantalla". La key llega ya tipada:
    // key.todoId es un Int, sin getInt("todoId") ni strings de por medio.
    val entryProvider = entryProvider<NavKey> {

        entry<TodoListDestination> {
            // hiltViewModel() pide el ViewModel al grafo: Hilt resuelve TODA la cadena
            // (casos de uso -> repositorio -> api + dao) sin que acá nombremos nada de eso.
            // Igual que antes, se crea UNA vez por entrada del back stack: sobrevive a
            // rotaciones y recomposiciones, y se destruye cuando la pantalla sale del stack.
            val viewModel: TodoListViewModel = hiltViewModel()
            TodoListRoute(
                viewModel = viewModel,
                onTodoClick = { todoId -> navigationState.navigate(TodoDetailDestination(todoId)) }
            )
        }

        entry<TodoDetailDestination> { key ->
            // El detalle necesita un dato que Hilt NO puede conocer: el id que eligió el
            // usuario. Se lo pasamos con la @AssistedFactory del ViewModel; el resto de
            // las dependencias las sigue poniendo el grafo.
            //
            // No hace falta un `key` string para distinguir instancias: el
            // rememberViewModelStoreNavEntryDecorator de abajo le da a cada entrada del
            // back stack su propio ViewModelStore (usa NavEntry.contentKey).
            val viewModel = hiltViewModel<TodoDetailViewModel, TodoDetailViewModel.Factory>(
                creationCallback = { factory -> factory.create(key.todoId) }
            )
            TodoDetailRoute(viewModel = viewModel, onBack = { navigationState.goBackFrom(key) })
        }

        entry<BookListDestination> {
            val viewModel: BookListViewModel = hiltViewModel()
            BookListRoute(
                viewModel = viewModel,
                onBookClick = { bookId -> navigationState.navigate(BookDetailDestination(bookId)) }
            )
        }

        entry<BookDetailDestination> { key ->
            val viewModel = hiltViewModel<BookDetailViewModel, BookDetailViewModel.Factory>(
                creationCallback = { factory -> factory.create(key.bookId) }
            )
            BookDetailRoute(viewModel = viewModel, onBack = { navigationState.goBackFrom(key) })
        }

        entry<PlaygroundDestination> {
            PlaygroundScreen()
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = navigationState::goBack,
        modifier = modifier
    )
}

/**
 * Convierte cada back stack en entradas "decoradas" y junta las de los tabs visibles.
 *
 * Decoramos TODOS los back stacks, aunque no se vean: así, al cambiar de tab,
 * las pantallas del otro tab conservan su ViewModel y su estado (scroll, formularios…).
 */
@Composable
private fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): List<NavEntry<NavKey>> {
    val entriesByTab = backStacks.mapValues { (_, backStack) ->
        rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = listOf(
                // Guarda el estado de rememberSaveable de cada pantalla
                rememberSaveableStateHolderNavEntryDecorator(),
                // Le da a cada pantalla su propio ViewModelStore
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider
        )
    }
    return visibleTabs.flatMap { tab -> entriesByTab.getValue(tab) }
}
