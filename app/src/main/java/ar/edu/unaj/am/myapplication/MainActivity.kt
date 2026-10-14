package ar.edu.unaj.am.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ar.edu.unaj.am.myapplication.core.ui.theme.ComposeAcademyTheme
import ar.edu.unaj.am.myapplication.ui.AppShell
import dagger.hilt.android.AndroidEntryPoint

/**
 * @AndroidEntryPoint: esta Activity puede pedirle dependencias a Hilt.
 * Acá no inyectamos nada directamente, pero es lo que habilita que los
 * hiltViewModel() de las pantallas encuentren el grafo.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeAcademyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppShell()
                }
            }
        }
    }
}
