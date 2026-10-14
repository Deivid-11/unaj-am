package ar.edu.unaj.am.myapplication.features.books.list

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unaj.am.myapplication.core.domain.model.Author
import ar.edu.unaj.am.myapplication.core.domain.model.Book
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.ui.components.ErrorContent
import ar.edu.unaj.am.myapplication.core.ui.components.LoadingContent
import ar.edu.unaj.am.myapplication.core.ui.components.asMessage
import ar.edu.unaj.am.myapplication.core.ui.theme.ComposeAcademyTheme
import ar.edu.unaj.am.myapplication.features.books.R
import ar.edu.unaj.am.myapplication.features.books.components.BookCover
import java.text.NumberFormat

// Cuántos ítems antes del final empezamos a pedir la página siguiente.
private const val LOAD_MORE_THRESHOLD = 5

@Composable
fun BookListRoute(
    viewModel: BookListViewModel,
    onBookClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BookListScreen(
        uiState = uiState,
        onBookClick = onBookClick,
        onLoadMore = viewModel::onLoadMore,
        onRetry = viewModel::onRetry
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    uiState: BookListUiState,
    onBookClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.books_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        val contentModifier = Modifier.padding(paddingValues)

        when (uiState) {
            is BookListUiState.Loading -> LoadingContent(
                message = stringResource(R.string.books_loading),
                modifier = contentModifier
            )

            is BookListUiState.Error -> ErrorContent(
                title = stringResource(R.string.books_error_title),
                message = uiState.error.asMessage(),
                onRetry = onRetry,
                modifier = contentModifier
            )

            is BookListUiState.Success -> BookList(
                state = uiState,
                onBookClick = onBookClick,
                onLoadMore = onLoadMore,
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun BookList(
    state: BookListUiState.Success,
    onBookClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    // derivedStateOf: el scroll cambia en cada frame, pero este Boolean solo cambia
    // cuando cruzamos el umbral. Así evitamos recomponer (y disparar el efecto) de más.
    val shouldLoadMore by remember(state.canLoadMore) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            state.canLoadMore && lastVisible >= listState.layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = state.books, key = { it.id }) { book ->
            BookCard(book = book, onClick = { onBookClick(book.id) })
        }

        // Footer: spinner mientras carga la página siguiente, o botón si falló.
        if (state.isLoadingMore || state.loadMoreError != null) {
            item(key = "footer") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.loadMoreError != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.loadMoreError.asMessage(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = onLoadMore) {
                                Text(stringResource(R.string.books_retry))
                            }
                        }
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BookCard(
    book: Book,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            BookCover(
                url = book.coverUrl,
                contentDescription = null,
                modifier = Modifier.size(width = 64.dp, height = 96.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.authors.joinToString { it.name }
                        .ifEmpty { stringResource(R.string.books_unknown_author) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                DownloadCount(count = book.downloadCount)
            }
        }
    }
}

@Composable
internal fun DownloadCount(count: Int, modifier: Modifier = Modifier) {
    // remember: NumberFormat es caro de crear; no hace falta hacerlo en cada recomposición.
    val formatter = remember { NumberFormat.getIntegerInstance() }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.books_downloads, formatter.format(count)),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ---------- Previews ----------

internal val previewBook = Book(
    id = 1342,
    title = "Pride and Prejudice",
    authors = listOf(Author("Jane Austen", 1775, 1817)),
    summary = "Elizabeth Bennet must learn to see past first impressions and hasty judgments.",
    subjects = listOf("Love stories", "England -- Fiction", "Sisters -- Fiction"),
    languages = listOf("en"),
    coverUrl = null,
    readUrl = null,
    downloadCount = 198164
)

@Composable
private fun BookListPreview(uiState: BookListUiState) {
    ComposeAcademyTheme {
        BookListScreen(uiState = uiState, onBookClick = {}, onLoadMore = {}, onRetry = {})
    }
}

@Preview(name = "Success", showBackground = true)
@Composable
private fun BookListSuccessPreview() = BookListPreview(
    BookListUiState.Success(
        books = listOf(previewBook, previewBook.copy(id = 2, title = "Frankenstein")),
        canLoadMore = true,
        isLoadingMore = true
    )
)

@Preview(name = "Load more error", showBackground = true)
@Composable
private fun BookListLoadMoreErrorPreview() = BookListPreview(
    BookListUiState.Success(
        books = listOf(previewBook),
        canLoadMore = true,
        loadMoreError = DomainError.Network
    )
)

@Preview(name = "Error", showBackground = true)
@Composable
private fun BookListErrorPreview() = BookListPreview(BookListUiState.Error(DomainError.Network))
