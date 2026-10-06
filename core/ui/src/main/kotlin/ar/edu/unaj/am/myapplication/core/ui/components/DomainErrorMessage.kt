package ar.edu.unaj.am.myapplication.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.ui.R

/**
 * El dominio dice QUÉ pasó ([DomainError]); la UI decide CÓMO contarlo.
 * Usar string resources permite traducir la app sin tocar ViewModels ni dominio.
 */
@Composable
fun DomainError.asMessage(): String = when (this) {
    DomainError.Network -> stringResource(R.string.core_ui_error_network)
    DomainError.NotFound -> stringResource(R.string.core_ui_error_not_found)
    DomainError.EmptyTitle -> stringResource(R.string.core_ui_error_empty_title)
    is DomainError.Unknown -> message ?: stringResource(R.string.core_ui_error_unknown)
}
