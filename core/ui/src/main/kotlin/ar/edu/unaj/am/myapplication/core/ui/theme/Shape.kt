package ar.edu.unaj.am.myapplication.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Las esquinas también son parte del tema.
 *
 * Cada componente de Material 3 toma su forma de acá según su tamaño: los Chip usan
 * `small`, los Card `medium`, los diálogos y bottom sheets `extraLarge`. Cambiar este
 * archivo redondea (o cuadra) toda la app de una sola vez.
 *
 * Los Button son la excepción: por defecto siempre son completamente redondeados,
 * sin importar estos valores.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
