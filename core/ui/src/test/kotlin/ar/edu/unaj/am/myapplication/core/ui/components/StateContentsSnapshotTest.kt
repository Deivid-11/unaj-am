package ar.edu.unaj.am.myapplication.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ar.edu.unaj.am.myapplication.core.ui.theme.AppTheme
import ar.edu.unaj.am.myapplication.core.ui.theme.ComposeAcademyTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * SNAPSHOT TESTS (o tests de regresión visual).
 *
 * Los otros tests verifican qué *hace* el código. Estos verifican cómo se *ve*:
 * dibujan el composable, lo guardan como imagen y, en cada corrida, comparan el
 * resultado contra esa imagen de referencia guardada en el repo.
 *
 *     ./gradlew :core:ui:recordRoborazziDebug    # (re)genera las imágenes de referencia
 *     ./gradlew :core:ui:verifyRoborazziDebug    # falla si algo cambió visualmente
 *     ./gradlew :core:ui:compareRoborazziDebug   # genera imágenes con las diferencias marcadas
 *
 * Las imágenes viven en core/ui/src/test/snapshots/images y SE COMMITEAN: son la
 * referencia. Si el cambio visual es intencional, se vuelve a grabar y se commitea la
 * imagen nueva; el diff del PR muestra exactamente qué cambió en pantalla.
 *
 * Por qué acá y no en un emulador: Robolectric permite dibujar en la JVM, así que
 * corren con `./gradlew test`, en segundos y sin dispositivo.
 *
 * Qué atrapan que los otros tests no: un color mal asignado, un padding roto, un texto
 * que se corta, un contraste ilegible en modo oscuro. Nada de eso lanza una excepción
 * ni rompe un assert: simplemente se ve mal.
 */
@RunWith(RobolectricTestRunner::class)
// GraphicsMode.NATIVE habilita el renderizado real (sin esto, la imagen sale en blanco).
// qualifiers fija tamaño de pantalla y densidad: la misma imagen en cualquier máquina.
// sdk = 34: Robolectric simula Android 14, NO la compileSdk del proyecto (37). Espresso,
// que Roborazzi usa por dentro, todavía no soporta los runtimes más nuevos. No afecta lo
// que estamos probando —el dibujo de un composable— y mantiene las imágenes estables.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xhdpi")
class StateContentsSnapshotTest {

    @Test
    fun loadingContent_light() = capture("loading_light") {
        LoadingContent(message = "Cargando libros…")
    }

    @Test
    fun loadingContent_dark() = capture("loading_dark", darkTheme = true) {
        LoadingContent(message = "Cargando tareas…")
    }

    @Test
    fun errorContent_light() = capture("error_light") {
        ErrorContent(
            title = "Ups, algo salió mal",
            message = "No se pudo conectar con el servidor",
            onRetry = {}
        )
    }

    /**
     * El caso que más justifica estos tests: un problema de contraste en modo oscuro no
     * rompe ningún test tradicional, pero deja la pantalla ilegible.
     */
    @Test
    fun errorContent_dark() = capture("error_dark", darkTheme = true) {
        ErrorContent(
            title = "Ups, algo salió mal",
            message = "No se pudo conectar con el servidor",
            onRetry = {}
        )
    }

    /** Los tokens propios del design system: si cambia AppSpacing, la imagen cambia. */
    @Test
    fun spacingTokens_light() = capture("spacing_light") {
        SpacingShowcase()
    }

    private fun capture(
        name: String,
        darkTheme: Boolean = false,
        content: @Composable () -> Unit
    ) {
        captureRoboImage(filePath = "src/test/snapshots/images/$name.png") {
            ComposeAcademyTheme(darkTheme = darkTheme) {
                Surface { content() }
            }
        }
    }
}

/** Barras proporcionales a cada token de espaciado. */
@Composable
private fun SpacingShowcase() {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        modifier = Modifier.padding(AppTheme.spacing.md)
    ) {
        val tokens = listOf(
            "xs" to AppTheme.spacing.xs,
            "sm" to AppTheme.spacing.sm,
            "md" to AppTheme.spacing.md,
            "lg" to AppTheme.spacing.lg,
            "xl" to AppTheme.spacing.xl
        )
        tokens.forEach { (name, value) ->
            Text(text = name, style = AppTheme.typography.labelSmall)
            Box(
                modifier = Modifier
                    .width(value)
                    .height(12.dp)
                    .background(AppTheme.colors.primary, AppTheme.shapes.extraSmall)
            )
        }
    }
}
