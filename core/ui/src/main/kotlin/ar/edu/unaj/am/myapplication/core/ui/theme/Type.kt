package ar.edu.unaj.am.myapplication.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ESCALA TIPOGRÁFICA.
 *
 * Material 3 define 15 estilos agrupados en 5 familias, de mayor a menor jerarquía:
 * display > headline > title > body > label. La regla práctica:
 *
 * - display/headline: títulos de pantalla.
 * - title: encabezados de sección, títulos de tarjeta.
 * - body: el texto que se lee de corrido.
 * - label: botones, chips, etiquetas chiquitas.
 *
 * En las pantallas nunca se escribe `fontSize = 18.sp`: se elige un estilo
 * (`MaterialTheme.typography.titleMedium`). Así la app tiene una jerarquía coherente
 * y respeta el tamaño de fuente que el usuario configuró en el sistema.
 *
 * Acá solo redefinimos los estilos que la app usa; el resto queda con el default de M3.
 * Para una fuente propia: poner el .ttf en res/font y cambiar este FontFamily.Default.
 */
private val AppFontFamily = FontFamily.Default

val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
