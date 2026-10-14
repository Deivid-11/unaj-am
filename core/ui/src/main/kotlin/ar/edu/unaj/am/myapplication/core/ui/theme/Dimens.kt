package ar.edu.unaj.am.myapplication.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * MaterialTheme trae colores, tipografías y formas. No trae espaciados, y los números
 * sueltos (`padding(16.dp)`) se desparraman por toda la app hasta que nadie sabe por qué
 * una pantalla respira distinto que otra.
 *
 * Esta es la forma estándar de EXTENDER el tema sin pelearse con Material:
 * un objeto de tokens + un CompositionLocal que lo hace visible en todo el árbol.
 */
data class AppSpacing(
    /** 4.dp — entre un ícono y su texto. */
    val xs: Dp = 4.dp,
    /** 8.dp — entre elementos de una misma fila o chip. */
    val sm: Dp = 8.dp,
    /** 16.dp — el espaciado por defecto: márgenes de pantalla y entre tarjetas. */
    val md: Dp = 16.dp,
    /** 24.dp — separación entre bloques distintos. */
    val lg: Dp = 24.dp,
    /** 32.dp — aire alrededor de estados vacíos o de error. */
    val xl: Dp = 32.dp
)

/**
 * staticCompositionLocalOf (y no compositionLocalOf) porque el valor no cambia durante
 * la vida de la app: si cambiara, Compose tendría que recomponer todo el árbol.
 *
 * El valor por defecto permite que un @Preview de un composable suelto no explote,
 * aunque lo correcto es envolverlo siempre en ComposeAcademyTheme.
 */
val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

/**
 * Punto de acceso único al design system:
 *
 *     Modifier.padding(AppTheme.spacing.md)
 *     color = AppTheme.colors.primary
 *     style = AppTheme.typography.titleLarge
 *
 * `AppTheme.colors` es exactamente `MaterialTheme.colorScheme`: no duplicamos nada,
 * solo le damos un nombre propio a la puerta de entrada para que mañana se pueda
 * sumar un token nuevo (elevaciones, duraciones de animación) sin tocar las pantallas.
 */
object AppTheme {

    val spacing: AppSpacing
        @Composable @ReadOnlyComposable get() = LocalAppSpacing.current

    val colors: ColorScheme
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes
}
