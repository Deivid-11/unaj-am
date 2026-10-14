package ar.edu.unaj.am.myapplication.features.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ar.edu.unaj.am.myapplication.core.ui.theme.AppTheme
import ar.edu.unaj.am.myapplication.core.ui.theme.ComposeAcademyTheme

/**
 * Vitrina del design system: la misma pantalla que usamos en clase para ver, en vivo,
 * qué cambia cuando se toca el tema.
 *
 * Nada de lo que hay acá abajo usa un color, un tamaño de letra o un padding literal:
 * todo sale de ComposeAcademyTheme. Probá en clase a cambiar un tono en Color.kt y mirá
 * cómo se actualiza toda la app sin tocar una sola pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundScreen(modifier: Modifier = Modifier) {
    // Los dos interruptores solo afectan a esta pantalla: más abajo volvemos a aplicar
    // el tema sobre este subárbol. Un tema anidado pisa al de afuera.
    val systemDark = isSystemInDarkTheme()
    var darkTheme by rememberSaveable { mutableStateOf(systemDark) }
    var dynamicColor by rememberSaveable { mutableStateOf(false) }

    ComposeAcademyTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        Surface(modifier = modifier.fillMaxSize(), color = AppTheme.colors.background) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.playground_title),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = AppTheme.colors.primaryContainer,
                            titleContentColor = AppTheme.colors.onPrimaryContainer
                        )
                    )
                }
            ) { paddingValues ->
                DesignSystemGallery(
                    darkTheme = darkTheme,
                    onDarkThemeChange = { darkTheme = it },
                    dynamicColor = dynamicColor,
                    onDynamicColorChange = { dynamicColor = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun DesignSystemGallery(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    dynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(AppTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                SwitchRow(
                    label = stringResource(R.string.playground_dark_mode),
                    checked = darkTheme,
                    onCheckedChange = onDarkThemeChange
                )
                SwitchRow(
                    label = stringResource(R.string.playground_dynamic_color),
                    checked = dynamicColor,
                    onCheckedChange = onDynamicColorChange
                )
            }
        }

        item {
            Section(
                title = stringResource(R.string.playground_section_colors),
                hint = stringResource(R.string.playground_colors_hint)
            ) {
                ColorRow("primary", AppTheme.colors.primary, AppTheme.colors.onPrimary)
                ColorRow("primaryContainer", AppTheme.colors.primaryContainer, AppTheme.colors.onPrimaryContainer)
                ColorRow("secondaryContainer", AppTheme.colors.secondaryContainer, AppTheme.colors.onSecondaryContainer)
                ColorRow("tertiaryContainer", AppTheme.colors.tertiaryContainer, AppTheme.colors.onTertiaryContainer)
                ColorRow("errorContainer", AppTheme.colors.errorContainer, AppTheme.colors.onErrorContainer)
                ColorRow("surfaceVariant", AppTheme.colors.surfaceVariant, AppTheme.colors.onSurfaceVariant)
                ColorRow("background", AppTheme.colors.background, AppTheme.colors.onBackground)
            }
        }

        item {
            Section(title = stringResource(R.string.playground_section_typography)) {
                TypeRow("headlineMedium", AppTheme.typography.headlineMedium)
                TypeRow("headlineSmall", AppTheme.typography.headlineSmall)
                TypeRow("titleLarge", AppTheme.typography.titleLarge)
                TypeRow("titleMedium", AppTheme.typography.titleMedium)
                TypeRow("bodyLarge", AppTheme.typography.bodyLarge)
                TypeRow("bodyMedium", AppTheme.typography.bodyMedium)
                TypeRow("labelLarge", AppTheme.typography.labelLarge)
            }
        }

        item {
            Section(title = stringResource(R.string.playground_section_shapes)) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                    ShapeSample("xs", AppTheme.shapes.extraSmall)
                    ShapeSample("sm", AppTheme.shapes.small)
                    ShapeSample("md", AppTheme.shapes.medium)
                    ShapeSample("lg", AppTheme.shapes.large)
                    ShapeSample("xl", AppTheme.shapes.extraLarge)
                }
            }
        }

        item {
            Section(
                title = stringResource(R.string.playground_section_spacing),
                hint = stringResource(R.string.playground_spacing_hint)
            ) {
                SpacingRow("xs", AppTheme.spacing.xs)
                SpacingRow("sm", AppTheme.spacing.sm)
                SpacingRow("md", AppTheme.spacing.md)
                SpacingRow("lg", AppTheme.spacing.lg)
                SpacingRow("xl", AppTheme.spacing.xl)
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    hint: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        Text(text = title, style = AppTheme.typography.titleLarge)
        if (hint != null) {
            Text(
                text = hint,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant
            )
        }
        HorizontalDivider(color = AppTheme.colors.outlineVariant)
        content()
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = AppTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ColorRow(name: String, container: Color, onContainer: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(container, AppTheme.shapes.small)
            .border(1.dp, AppTheme.colors.outlineVariant, AppTheme.shapes.small)
            .padding(AppTheme.spacing.md)
    ) {
        Text(text = name, style = AppTheme.typography.labelLarge, color = onContainer)
    }
}

@Composable
private fun TypeRow(name: String, style: TextStyle) {
    Column {
        Text(
            text = name,
            style = AppTheme.typography.labelSmall,
            color = AppTheme.colors.onSurfaceVariant
        )
        Text(text = "Compose Academy", style = style)
    }
}

@Composable
private fun ShapeSample(name: String, shape: Shape) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(AppTheme.colors.secondaryContainer, shape)
        )
        Text(
            text = name,
            style = AppTheme.typography.labelSmall,
            color = AppTheme.colors.onSurfaceVariant
        )
    }
}

@Composable
private fun SpacingRow(name: String, value: Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        Text(
            text = name,
            style = AppTheme.typography.labelLarge,
            modifier = Modifier.width(32.dp)
        )
        Box(
            modifier = Modifier
                .width(value)
                .height(16.dp)
                .background(AppTheme.colors.primary, AppTheme.shapes.extraSmall)
        )
        Text(
            text = "$value",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Claro")
@Composable
private fun PlaygroundPreview() {
    ComposeAcademyTheme {
        PlaygroundScreen()
    }
}

@Preview(showBackground = true, name = "Oscuro")
@Composable
private fun PlaygroundDarkPreview() {
    ComposeAcademyTheme(darkTheme = true) {
        PlaygroundScreen()
    }
}
