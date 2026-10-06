package ar.edu.unaj.am.myapplication.features.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.unaj.am.myapplication.core.ui.theme.MyApplicationTheme
import ar.edu.unaj.am.myapplication.core.ui.theme.Typography

/**
 * Pantalla en blanco para hacer pruebas en vivo en clase.
 * Todo lo que escribas adentro de [PlaygroundContent] aparece en el tab "Playground"
 * y también en el @Preview de abajo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.playground_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {

                }
            )
        }
    ) { paddingValues ->
        PlaygroundContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
private fun PlaygroundContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        var contador by remember { mutableStateOf(0) }

        Contador(
            value = contador,
            onclick = { contador++ }
        )

        Contador(
            value = contador,
            onclick = { contador-- }
        )
    }
}

@Composable
fun Contador(value: Int, onclick: () -> Unit) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {

        Column(
            modifier = Modifier.background(Color.Green).padding(32.dp)
        ) {
            Text(
                text = "contadorA: $value",
                style = Typography.headlineSmall
            )
        }
        Button(onClick = {
            onclick()
        }) {
            Text("Sumar")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaygroundPreview() {
    MyApplicationTheme(dynamicColor = false) {
        PlaygroundScreen()
    }
}
