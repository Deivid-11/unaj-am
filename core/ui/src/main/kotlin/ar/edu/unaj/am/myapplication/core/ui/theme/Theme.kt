package ar.edu.unaj.am.myapplication.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

/**
 * ESQUEMA DE COLOR = qué tono de la paleta cumple cada rol.
 *
 * Los roles vienen de a pares: `primary` es el color de fondo de algo importante y
 * `onPrimary` es el color del texto/ícono que va ENCIMA. Si siempre se usan en pareja,
 * el contraste está garantizado en claro y en oscuro.
 *
 * Los "container" son la versión suave del rol: mismo significado, menos protagonismo.
 */
private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    onPrimary = White,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal10,
    secondary = SlateTeal40,
    onSecondary = White,
    secondaryContainer = SlateTeal90,
    onSecondaryContainer = SlateTeal10,
    tertiary = Blue40,
    onTertiary = White,
    tertiaryContainer = Blue90,
    onTertiaryContainer = Blue10,
    error = Red40,
    onError = White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    // Escala de superficies: cuanto más "arriba" está un elemento (barra, tarjeta,
    // diálogo), más alto es su contenedor. Reemplaza a las sombras de Material 2.
    surfaceDim = Neutral87,
    surfaceBright = Neutral99,
    surfaceContainerLowest = White,
    surfaceContainerLow = Neutral96,
    surfaceContainer = Neutral95,
    surfaceContainerHigh = Neutral92,
    surfaceContainerHighest = Neutral90,
    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    inversePrimary = Teal80,
    scrim = Black
)

/**
 * El modo oscuro NO es "el claro con los colores invertidos": usa tonos más claros
 * para el color de marca (Teal80 en vez de Teal40) sobre fondos oscuros, porque un
 * color saturado sobre negro cansa la vista y pierde contraste.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal20,
    primaryContainer = Teal30,
    onPrimaryContainer = Teal90,
    secondary = SlateTeal80,
    onSecondary = SlateTeal20,
    secondaryContainer = SlateTeal30,
    onSecondaryContainer = SlateTeal90,
    tertiary = Blue80,
    onTertiary = Blue20,
    tertiaryContainer = Blue30,
    onTertiaryContainer = Blue90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    surfaceDim = Neutral8,
    surfaceBright = Neutral24,
    surfaceContainerLowest = Neutral6,
    surfaceContainerLow = Neutral10,
    surfaceContainer = Neutral12,
    surfaceContainerHigh = Neutral17,
    surfaceContainerHighest = Neutral22,
    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral10,
    inversePrimary = Teal40,
    scrim = Black
)

/**
 * Tema compartido: vive en :core:ui para que todas las features se vean igual.
 *
 * Es un composable que no dibuja nada: solo deja valores "en el aire" (vía
 * CompositionLocal) para que cualquier composable de adentro los lea con
 * MaterialTheme.* o AppTheme.*, sin pasarlos por parámetro pantalla por pantalla.
 *
 * @param dynamicColor en Android 12+ el sistema puede generar una paleta a partir del
 * fondo de pantalla del usuario. Queda APAGADO por defecto: es una función linda, pero
 * reemplaza por completo la paleta de marca, y acá justamente queremos ver la nuestra.
 * En clase: ponelo en true desde el Playground y mirá cómo cambia toda la app.
 */
@Composable
fun ComposeAcademyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Los tokens propios viajan igual que los de Material: por el árbol de composición.
    CompositionLocalProvider(LocalAppSpacing provides AppSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
