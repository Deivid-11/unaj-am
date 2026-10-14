package ar.edu.unaj.am.myapplication.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PALETA TONAL (Material 3).
 *
 * En M3 no se eligen "un color primario y uno secundario": se parte de un color semilla
 * y se genera una rampa de tonos del 0 (negro) al 100 (blanco) para cada familia.
 * El número del nombre es el tono: Teal40 es el tono 40 de la familia teal.
 *
 * Esos tonos NO se usan directamente en las pantallas: se asignan a ROLES
 * (primary, onPrimary, surface…) en Theme.kt. El modo claro y el oscuro usan la misma
 * rampa con tonos distintos, y por eso el contraste se mantiene en los dos.
 *
 * Para generar otra paleta: Material Theme Builder (m3.material.io/theme-builder).
 */

// --- Primary: el color de marca. Acciones principales, estados seleccionados. ---
val Teal10 = Color(0xFF002020)
val Teal20 = Color(0xFF003737)
val Teal30 = Color(0xFF004F50)
val Teal40 = Color(0xFF006A6A)
val Teal80 = Color(0xFF4CDADA)
val Teal90 = Color(0xFF6FF7F6)

// --- Secondary: acompaña al primario con menos peso (chips, filtros). ---
val SlateTeal10 = Color(0xFF051F1F)
val SlateTeal20 = Color(0xFF1B3534)
val SlateTeal30 = Color(0xFF324B4B)
val SlateTeal40 = Color(0xFF4A6363)
val SlateTeal80 = Color(0xFFB0CCCB)
val SlateTeal90 = Color(0xFFCCE8E7)

// --- Tertiary: contrapunto para destacar algo puntual sin romper la armonía. ---
val Blue10 = Color(0xFF041C35)
val Blue20 = Color(0xFF1C314B)
val Blue30 = Color(0xFF334863)
val Blue40 = Color(0xFF4B607C)
val Blue80 = Color(0xFFB3C8E8)
val Blue90 = Color(0xFFD3E4FF)

// --- Error: es un rol más del tema, no un Color.Red suelto en medio de una pantalla. ---
val Red10 = Color(0xFF410002)
val Red20 = Color(0xFF690005)
val Red30 = Color(0xFF93000A)
val Red40 = Color(0xFFBA1A1A)
val Red80 = Color(0xFFFFB4AB)
val Red90 = Color(0xFFFFDAD6)

// --- Neutral: fondos y textos. Casi toda la pantalla es neutral. ---
// Hay muchos tonos tan juntos (87, 92, 94, 96…) porque Material 3 usa una escala de
// superficies: barras, tarjetas y diálogos se diferencian por capas apenas distintas
// de gris, no por sombras. Si no se definen, los componentes caen en el violeta
// por defecto de Material: eso se nota enseguida en la barra de navegación.
val Neutral6 = Color(0xFF0B0F0F)
val Neutral8 = Color(0xFF101414)
val Neutral10 = Color(0xFF191C1C)
val Neutral12 = Color(0xFF1D2020)
val Neutral17 = Color(0xFF272B2B)
val Neutral20 = Color(0xFF2D3131)
val Neutral22 = Color(0xFF323535)
val Neutral24 = Color(0xFF363A3A)
val Neutral87 = Color(0xFFDDE4E3)
val Neutral90 = Color(0xFFE0E3E2)
val Neutral92 = Color(0xFFE9ECEB)
val Neutral95 = Color(0xFFEFF1F0)
val Neutral96 = Color(0xFFF4F7F6)
val Neutral99 = Color(0xFFFAFDFC)

// --- Neutral variant: bordes, divisores y superficies de apoyo. ---
val NeutralVariant30 = Color(0xFF3F4949)
val NeutralVariant50 = Color(0xFF6F7979)
val NeutralVariant60 = Color(0xFF889392)
val NeutralVariant80 = Color(0xFFBEC9C8)
val NeutralVariant90 = Color(0xFFDAE5E4)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
