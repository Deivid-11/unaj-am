package ar.edu.unaj.am.myapplication.core.network.fake

import ar.edu.unaj.am.myapplication.core.network.HttpException
import ar.edu.unaj.am.myapplication.core.network.TodoApi
import ar.edu.unaj.am.myapplication.core.network.model.TodoDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

/**
 * "Servidor" en memoria que simula latencia y errores de red.
 * Como implementa [TodoApi], el resto de la app no sabe (ni le importa) que es falso:
 * cambiarlo por Retrofit es tocar UNA línea en AppContainer.
 */
class FakeTodoApi(
    private val latencyMs: Long = 500
) : TodoApi {

    private val mutex = Mutex()
    private var nextId = 16

    // Cada tarea explica un concepto de la clase: el detalle funciona como "apunte".
    private val serverTodos = mutableListOf(
        TodoDto(
            id = 1,
            title = "Composable y Composition",
            description = "Una función @Composable describe cómo se ve una parte de la pantalla a partir de datos: no crea vistas, emite UI." +
                "\n\n" +
                "Cuando Compose ejecuta los composables por primera vez arma un árbol llamado Composition: la \"foto\" de la UI actual. A diferencia de las Views con XML, no modificamos la pantalla a mano (setText, setVisibility): cambiamos los datos y Compose redibuja." +
                "\n\n" +
                "Regla de oro: un composable debería ser como una función pura. Mismos parámetros, misma UI, y sin efectos secundarios en su cuerpo.",
            completed = false
        ),
        TodoDto(
            id = 2,
            title = "Recomposition",
            description = "Recomposition es cuando Compose vuelve a ejecutar un composable porque cambió un estado que ese composable lee." +
                "\n\n" +
                "Compose es inteligente: solo recompone las partes afectadas y saltea (skip) las funciones cuyos parámetros no cambiaron. Por eso conviene pasar datos inmutables (data class con val, List en vez de MutableList)." +
                "\n\n" +
                "Consecuencia práctica: un composable puede ejecutarse muchas veces, en cualquier orden. Nunca hagas trabajo pesado ni llamadas a red directamente en su cuerpo.",
            completed = false
        ),
        TodoDto(
            id = 3,
            title = "remember y rememberSaveable",
            description = "Como un composable se re-ejecuta en cada recomposición, cualquier variable local se perdería. remember guarda un valor en la Composition y lo devuelve en las siguientes ejecuciones." +
                "\n\n" +
                "remember sobrevive a la recomposición, pero NO a la rotación de pantalla ni a la muerte del proceso. Para eso existe rememberSaveable, que guarda el valor en el Bundle (así funcionan los diálogos de esta app: rotá la pantalla con uno abierto)." +
                "\n\n" +
                "Usalos para estado efímero de UI: si un diálogo está abierto, qué tab está seleccionado, el texto de un campo.",
            completed = false
        ),
        TodoDto(
            id = 4,
            title = "mutableStateOf y State",
            description = "mutableStateOf crea un valor observable: cuando cambia, Compose se entera y recompone a quien lo lea." +
                "\n\n" +
                "Casi siempre va junto a remember:  var abierto by remember { mutableStateOf(false) }. El \"by\" es delegación de Kotlin: nos deja leer y escribir abierto como si fuera una variable normal." +
                "\n\n" +
                "Ojo: si guardás una MutableList dentro de un mutableStateOf y le hacés add(), Compose NO se entera, porque la referencia no cambió. Reemplazá la lista entera o usá mutableStateListOf.",
            completed = false
        ),
        TodoDto(
            id = 5,
            title = "State hoisting",
            description = "State hoisting (\"elevar el estado\") es sacar el estado de un composable y recibirlo por parámetro, junto con lambdas para avisar eventos: value + onValueChange." +
                "\n\n" +
                "En esta app cada pantalla tiene dos partes. TodoListRoute es stateful: conoce al ViewModel y lee su estado. TodoListScreen es stateless: solo recibe datos y lambdas." +
                "\n\n" +
                "Ventajas del composable stateless: se puede ver en @Preview, testear sin ViewModel y reutilizar. Subí el estado hasta el ancestro común más bajo que lo necesite, ni más arriba ni más abajo.",
            completed = false
        ),
        TodoDto(
            id = 6,
            title = "Unidirectional Data Flow (UDF)",
            description = "El estado baja y los eventos suben. El ViewModel expone un UiState, la UI lo dibuja, y cuando el usuario hace algo la UI llama a una función del ViewModel (onToggleTodo, onSave…)." +
                "\n\n" +
                "El ViewModel actualiza su estado, el nuevo estado baja y la UI se recompone. Nunca al revés: la UI no modifica el estado directamente." +
                "\n\n" +
                "Beneficio: hay una sola fuente de verdad y el flujo es predecible. Si algo se ve mal, alcanza con mirar qué estado llegó.",
            completed = false
        ),
        TodoDto(
            id = 7,
            title = "ViewModel y StateFlow",
            description = "El ViewModel guarda el estado de la pantalla y sobrevive a los cambios de configuración (rotación, modo oscuro). La Activity se destruye y se recrea; el ViewModel no." +
                "\n\n" +
                "Patrón que usamos: un MutableStateFlow privado (solo el ViewModel escribe) y un StateFlow público de solo lectura para la UI. Las corrutinas se lanzan en viewModelScope, que se cancela solo cuando la pantalla sale del back stack." +
                "\n\n" +
                "El ViewModel no conoce la UI: no recibe Context, no tiene referencias a composables. Por eso se testea con JUnit puro (mirá TodoListViewModelTest).",
            completed = false
        ),
        TodoDto(
            id = 8,
            title = "collectAsStateWithLifecycle",
            description = "Convierte un Flow en un State de Compose para que la UI se recomponga con cada emisión." +
                "\n\n" +
                "Frente a collectAsState, la versión WithLifecycle deja de recolectar cuando la app pasa a segundo plano (por debajo de STARTED) y vuelve a hacerlo al regresar. Ahorra batería y trabajo innecesario." +
                "\n\n" +
                "Es la forma recomendada de leer el estado del ViewModel en Android:  val uiState by viewModel.uiState.collectAsStateWithLifecycle()",
            completed = false
        ),
        TodoDto(
            id = 9,
            title = "Side effects: LaunchedEffect",
            description = "Un side effect es algo que escapa del mundo de Compose: mostrar un snackbar, navegar, loguear. No puede ir en el cuerpo del composable porque se repetiría en cada recomposición." +
                "\n\n" +
                "LaunchedEffect(key) lanza una corrutina al entrar en la Composition, y la cancela y relanza solo cuando cambia la key. En el detalle lo usamos así: cuando isSaved pasa a true, volvemos a la lista." +
                "\n\n" +
                "Otros efectos útiles: DisposableEffect (registrar y limpiar listeners), rememberCoroutineScope (lanzar desde un onClick) y SideEffect.",
            completed = false
        ),
        TodoDto(
            id = 10,
            title = "Modifiers",
            description = "Un Modifier decora un composable: tamaño, padding, fondo, clicks, scroll, animaciones. Se encadenan con punto y el ORDEN importa." +
                "\n\n" +
                "Modifier.padding(16.dp).background(Color.Red) no es lo mismo que Modifier.background(Color.Red).padding(16.dp): en el primero el padding queda afuera del fondo, en el segundo adentro." +
                "\n\n" +
                "Buena práctica: todo composable reutilizable recibe modifier: Modifier = Modifier como primer parámetro opcional y lo aplica a su elemento raíz.",
            completed = false
        ),
        TodoDto(
            id = 11,
            title = "LazyColumn y keys",
            description = "LazyColumn es el reemplazo del RecyclerView: solo compone los ítems visibles en pantalla." +
                "\n\n" +
                "Pasar key = { it.id } le dice a Compose qué ítem es cuál aunque cambie el orden. Así conserva el estado de cada fila y puede animar el movimiento con Modifier.animateItem(). Probá marcar una tarea como completada: baja al final de la lista animada." +
                "\n\n" +
                "Sin keys, Compose identifica los ítems por posición: si insertás uno arriba, todos los de abajo \"cambian\" y se recomponen.",
            completed = false
        ),
        TodoDto(
            id = 12,
            title = "Navegación type-safe",
            description = "Navigation Compose maneja el back stack de pantallas. Desde la versión 2.8 los destinos son tipos de Kotlin marcados con @Serializable, en vez de Strings." +
                "\n\n" +
                "Antes:  navigate(\"detail/\$id\") y getInt(\"todoId\"). Un typo compilaba y explotaba en runtime. Ahora:  navigate(TodoDetailDestination(todoId = id)) y backStackEntry.toRoute<TodoDetailDestination>(). Si le pasás un tipo incorrecto, no compila." +
                "\n\n" +
                "Buena práctica: las pantallas no conocen al NavController. Exponen callbacks (onTodoClick, onBack) y la navegación se decide en un solo lugar (AppNavHost).",
            completed = false
        ),
        TodoDto(
            id = 13,
            title = "Sealed classes para el UiState",
            description = "Una sealed interface define un conjunto cerrado de estados: Loading, Success o Error. La pantalla está en uno solo a la vez." +
                "\n\n" +
                "El when sobre una sealed es exhaustivo: si mañana agregamos un estado Empty y no lo manejamos en la UI, el proyecto no compila. Es imposible olvidarse un caso." +
                "\n\n" +
                "Tocá la tarea \"ERROR: Tarea corrupta\" para ver el estado Error del detalle, con su botón Reintentar.",
            completed = false
        ),
        TodoDto(
            id = 14,
            title = "Clean Architecture",
            description = "Separamos el código en capas con responsabilidades claras. Presentación (:features:*): pantallas y ViewModels. Dominio (:core:domain): modelos, contratos de repositorio y casos de uso, en Kotlin puro. Datos (:core:data, :core:network, :core:database): de dónde vienen los datos." +
                "\n\n" +
                "Regla de dependencias: las capas externas dependen de las internas, nunca al revés. Las features solo conocen el dominio; no saben si los datos vienen de Retrofit, de Room o, como hoy, de un fake en memoria." +
                "\n\n" +
                "Por eso cambiar FakeTodoApi por una API real es tocar una sola línea en AppContainer.",
            completed = false
        ),
        TodoDto(
            id = 15,
            title = "Crear un CRUD completo",
            description = "Esta app combina todo lo anterior en un Master-Detail: listar, crear, editar, completar y borrar tareas." +
                "\n\n" +
                "Fijate que al editar en el detalle y volver, la lista ya está actualizada sin recargar nada. Las dos pantallas observan la misma fuente de verdad (el DAO) a través del repositorio: offline-first." +
                "\n\n" +
                "Siguiente paso: reemplazar los fakes por Retrofit y Room, e incorporar Hilt para la inyección de dependencias.",
            completed = true
        ),
        // Esta tarea fuerza un error de red al abrir su detalle:
        // sirve para mostrar cómo la sealed class maneja el estado Error.
        TodoDto(
            id = ERROR_TODO_ID,
            title = "ERROR: Tarea corrupta",
            description = "Forzar estado de Error en el detalle",
            completed = false
        )
    )

    override suspend fun getTodos(): List<TodoDto> = request { serverTodos.toList() }

    override suspend fun getTodo(id: Int): TodoDto = request {
        if (id == ERROR_TODO_ID) throw IOException("No se pudo conectar con el servidor")
        serverTodos.find { it.id == id } ?: throw HttpException(404, "Todo $id not found")
    }

    override suspend fun createTodo(title: String, description: String): TodoDto = request {
        TodoDto(nextId++, title, description, completed = false).also { serverTodos.add(it) }
    }

    override suspend fun updateTodo(todo: TodoDto): TodoDto = request {
        val index = serverTodos.indexOfFirst { it.id == todo.id }
        if (index == -1) throw HttpException(404, "Todo ${todo.id} not found")
        serverTodos[index] = todo
        todo
    }

    override suspend fun deleteTodo(id: Int) = request {
        if (!serverTodos.removeAll { it.id == id }) throw HttpException(404, "Todo $id not found")
    }

    private suspend fun <T> request(block: () -> T): T {
        delay(latencyMs)
        return mutex.withLock(action = block)
    }

    companion object {
        const val ERROR_TODO_ID = 99
    }
}
