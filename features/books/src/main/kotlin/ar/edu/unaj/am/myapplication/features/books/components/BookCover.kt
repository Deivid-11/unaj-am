package ar.edu.unaj.am.myapplication.features.books.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage

/**
 * Coil descarga, cachea y decodifica la imagen en segundo plano.
 * Mientras carga (o si no hay portada) mostramos un placeholder.
 */
@Composable
internal fun BookCover(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val shapedModifier = modifier.clip(RoundedCornerShape(8.dp))
    if (url == null) {
        CoverPlaceholder(shapedModifier)
        return
    }
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = shapedModifier,
        loading = { CoverPlaceholder(Modifier.matchParentSize()) },
        error = { CoverPlaceholder(Modifier.matchParentSize()) }
    )
}

@Composable
private fun CoverPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
