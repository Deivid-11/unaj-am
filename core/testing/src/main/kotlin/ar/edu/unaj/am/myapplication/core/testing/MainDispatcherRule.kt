package ar.edu.unaj.am.myapplication.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * `viewModelScope` usa Dispatchers.Main, que en la JVM no existe (es el hilo de UI de
 * Android). Esta regla lo reemplaza por un dispatcher de test antes de cada @Test
 * y lo restaura después.
 *
 * Antes esto estaba copiado en cada test de ViewModel con @Before/@After; una regla
 * de JUnit es el lugar correcto para una preparación que se repite.
 *
 * Uso:
 *
 *     @get:Rule val mainDispatcherRule = MainDispatcherRule()
 *
 * Por defecto usa UnconfinedTestDispatcher: las corrutinas se ejecutan apenas se lanzan,
 * así el test puede mirar el estado final sin esperar nada.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
