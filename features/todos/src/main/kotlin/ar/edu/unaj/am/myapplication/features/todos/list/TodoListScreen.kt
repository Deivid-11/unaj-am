package ar.edu.unaj.am.myapplication.features.todos.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.ui.components.ErrorContent
import ar.edu.unaj.am.myapplication.core.ui.components.LoadingContent
import ar.edu.unaj.am.myapplication.core.ui.components.MessageContent
import ar.edu.unaj.am.myapplication.core.ui.components.asMessage
import ar.edu.unaj.am.myapplication.core.ui.theme.MyApplicationTheme
import ar.edu.unaj.am.myapplication.features.todos.R
import kotlinx.coroutines.launch

// Tarea "trampa" del FakeTodoApi: la resaltamos para que en clase sea fácil encontrarla.
private const val ERROR_DEMO_TODO_ID = 99

/**
 * "Route" = composable CON estado. Es el único que conoce al ViewModel:
 * lee el estado y conecta los eventos. Todo lo demás es UI pura.
 */
@Composable
fun TodoListRoute(
    viewModel: TodoListViewModel,
    onTodoClick: (Int) -> Unit
) {
    // collectAsStateWithLifecycle deja de escuchar cuando la app pasa a background.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TodoListScreen(
        uiState = uiState,
        onTodoClick = onTodoClick,
        onToggle = viewModel::onToggleTodo,
        onDelete = viewModel::onDeleteTodo,
        onAdd = viewModel::onAddTodo,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onUserMessageShown = viewModel::onUserMessageShown
    )
}

/**
 * "Screen" = composable SIN estado (stateless). Recibe datos y avisa eventos con lambdas.
 * Ventajas: se puede previsualizar (@Preview), testear y reutilizar sin un ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    uiState: TodoListUiState,
    onTodoClick: (Int) -> Unit,
    onToggle: (Todo) -> Unit,
    onDelete: (Int) -> Unit,
    onAdd: (title: String, description: String) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onUserMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado efímero de UI (qué diálogo está abierto) → vive en la UI, no en el ViewModel.
    // rememberSaveable además sobrevive a la rotación de pantalla.
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var todoIdToDelete by rememberSaveable { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Efecto disparado por estado: cuando aparece un mensaje, se muestra y se "consume".
    val userMessage = (uiState as? TodoListUiState.Success)?.userMessage
    val userMessageText = userMessage?.asMessage()
    LaunchedEffect(userMessage) {
        if (userMessageText != null) {
            snackbarHostState.showSnackbar(userMessageText)
            onUserMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.todos_title), fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState is TodoListUiState.Success) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.todos_add)
                    )
                }
            }
        }
    ) { paddingValues ->
        val contentModifier = Modifier.padding(paddingValues)

        // when exhaustivo: el compilador obliga a manejar TODOS los estados.
        when (uiState) {
            is TodoListUiState.Loading -> LoadingContent(
                message = stringResource(R.string.todos_loading),
                modifier = contentModifier
            )

            is TodoListUiState.Error -> ErrorContent(
                title = stringResource(R.string.todos_error_title),
                message = uiState.error.asMessage(),
                onRetry = onRetry,
                modifier = contentModifier
            )

            is TodoListUiState.Success -> PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = contentModifier
            ) {
                if (uiState.todos.isEmpty()) {
                    MessageContent(
                        icon = Icons.AutoMirrored.Filled.List,
                        title = stringResource(R.string.todos_empty_title),
                        message = stringResource(R.string.todos_empty_message)
                    )
                } else {
                    TodoList(
                        todos = uiState.todos,
                        onTodoClick = onTodoClick,
                        onToggle = onToggle,
                        onDelete = { todoIdToDelete = it }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, description ->
                onAdd(title, description)
                showAddDialog = false
            }
        )
    }

    todoIdToDelete?.let { todoId ->
        DeleteConfirmDialog(
            onConfirm = {
                onDelete(todoId)
                todoIdToDelete = null
            },
            onDismiss = { todoIdToDelete = null }
        )
    }
}

@Composable
private fun TodoList(
    todos: List<Todo>,
    onTodoClick: (Int) -> Unit,
    onToggle: (Todo) -> Unit,
    onDelete: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // key = id: Compose sabe qué ítem es cuál aunque cambie el orden,
        // así conserva su estado y puede animar el movimiento (animateItem).
        items(items = todos, key = { it.id }) { todo ->
            SwipeableTodoItem(
                todo = todo,
                onClick = { onTodoClick(todo.id) },
                onToggle = { onToggle(todo) },
                onDelete = { onDelete(todo.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun SwipeableTodoItem(
    todo: Todo,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()
    // rememberCoroutineScope: para lanzar corrutinas desde un callback (acá, animar la fila de vuelta).
    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        onDismiss = {
            onDelete()
            // No la borramos todavía: primero pedimos confirmación, así que la fila vuelve a su lugar.
            scope.launch { dismissState.reset() }
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.todos_delete),
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    ) {
        TodoCard(todo = todo, onClick = onClick, onToggle = onToggle)
    }
}

@Composable
private fun TodoCard(
    todo: Todo,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    val isErrorDemoItem = todo.id == ERROR_DEMO_TODO_ID

    val containerColor by animateColorAsState(
        targetValue = when {
            isErrorDemoItem -> MaterialTheme.colorScheme.errorContainer
            todo.isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "cardColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (todo.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
            ) {
                Icon(
                    imageVector = if (todo.isCompleted) Icons.Default.Check else Icons.Outlined.Circle,
                    contentDescription = stringResource(
                        if (todo.isCompleted) R.string.todos_completed else R.string.todos_pending
                    ),
                    tint = if (todo.isCompleted) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = when {
                        isErrorDemoItem -> MaterialTheme.colorScheme.error
                        todo.isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = if (isErrorDemoItem) {
                        stringResource(R.string.todos_error_item_hint)
                    } else {
                        todo.description
                    },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isErrorDemoItem) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun AddTodoDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    // Estado local del formulario: nadie más lo necesita, así que no sube al ViewModel.
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.todos_dialog_new_title), fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.todos_field_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.todos_field_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(title, description) }, enabled = title.isNotBlank()) {
                Text(stringResource(R.string.todos_dialog_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.todos_dialog_cancel))
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.todos_delete_title)) },
        text = { Text(stringResource(R.string.todos_delete_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.todos_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.todos_dialog_cancel))
            }
        }
    )
}

// ---------- Previews: posibles porque TodoListScreen no depende del ViewModel ----------

private val previewTodos = listOf(
    Todo(1, "Aprender remember", "Preservar estado entre recomposiciones", isCompleted = false),
    Todo(2, "Estudiar ViewModel", "Separar lógica de la UI con StateFlow", isCompleted = false),
    Todo(3, "Crear un CRUD", "Combinar todo en una app Master-Detail", isCompleted = true)
)

@Composable
private fun TodoListPreview(uiState: TodoListUiState) {
    MyApplicationTheme(dynamicColor = false) {
        TodoListScreen(
            uiState = uiState,
            onTodoClick = {},
            onToggle = {},
            onDelete = {},
            onAdd = { _, _ -> },
            onRefresh = {},
            onRetry = {},
            onUserMessageShown = {}
        )
    }
}

@Preview(name = "Success", showBackground = true)
@Composable
private fun TodoListSuccessPreview() = TodoListPreview(TodoListUiState.Success(previewTodos))

@Preview(name = "Empty", showBackground = true)
@Composable
private fun TodoListEmptyPreview() = TodoListPreview(TodoListUiState.Success(emptyList()))

@Preview(name = "Loading", showBackground = true)
@Composable
private fun TodoListLoadingPreview() = TodoListPreview(TodoListUiState.Loading)

@Preview(name = "Error", showBackground = true)
@Composable
private fun TodoListErrorPreview() = TodoListPreview(TodoListUiState.Error(DomainError.Network))
