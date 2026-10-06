package ar.edu.unaj.am.myapplication.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Science
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import ar.edu.unaj.am.myapplication.R

/**
 * Los tabs de la bottom bar. Cada uno tiene su propio back stack, que arranca en [startKey].
 * Agregar un tab nuevo = agregar una entrada acá y sus pantallas en AppNavDisplay.
 */
enum class TopLevelDestination(
    val startKey: NavKey,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val label: Int
) {
    TODOS(
        startKey = TodoListDestination,
        selectedIcon = Icons.Filled.Checklist,
        unselectedIcon = Icons.Outlined.Checklist,
        label = R.string.tab_todos
    ),
    BOOKS(
        startKey = BookListDestination,
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        label = R.string.tab_books
    ),
    PLAYGROUND(
        startKey = PlaygroundDestination,
        selectedIcon = Icons.Filled.Science,
        unselectedIcon = Icons.Outlined.Science,
        label = R.string.tab_playground
    )
}
