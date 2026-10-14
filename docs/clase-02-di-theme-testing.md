# Clase 2 — Inyección de dependencias (Hilt), theming y testing

Guía para dictar la clase. El código ya está aplicado en el repo: esto explica **en qué orden
mostrarlo**, qué decir en cada paso y qué errores van a aparecer.

- Punto de partida: `git checkout clase-01-ui-mvvm`
- Punto de llegada: este commit

**Los tres temas se apoyan en la misma idea.** Inyectar dependencias es pasar parámetros por
constructor; un theme es pasar valores por el árbol de composición en vez de repetirlos; y un
test es poder reemplazar esos parámetros por otros. Si en algún momento la clase se siente como
tres temas sueltos, volvé a esa frase.

---

## Parte 1 — De `AppContainer` a Hilt (~50 min)

### 1.1 El problema (mostrar el "antes")

Abrir `di/AppContainer.kt` tal como estaba en la clase 1:

```kotlin
class AppContainer {
    private val todoApi: TodoApi = FakeTodoApi()
    private val todoDao: TodoDao = InMemoryTodoDao()
    private val todoRepository: TodoRepository = TodoRepositoryImpl(todoApi, todoDao)

    val observeTodos = ObserveTodosUseCase(todoRepository)
    val refreshTodos = RefreshTodosUseCase(todoRepository)
    // … 7 más
}
```

Preguntas para hacerle al curso antes de mostrar la solución:

1. ¿Qué pasa cuando agregamos una feature nueva? (el archivo crece y hay que acordarse de tocarlo)
2. ¿Cómo nos aseguramos de que la lista y el detalle compartan el mismo repositorio? (hoy: a mano,
   porque está declarado una sola vez; si alguien escribe `TodoRepositoryImpl(...)` en otro lado,
   se rompe en silencio)
3. ¿Y el `container` que viaja `AppShell → AppNavDisplay → cada entry`? Es *plumbing*: lo pasamos por
   pantallas que no lo usan.

**La idea clave:** el problema no es crear objetos, es **mantener a mano el grafo de quién necesita a quién**.
Hilt no inventa nada nuevo: genera ese mismo código.

### 1.2 Las anotaciones, de a una

Mostrar en este orden, compilando entre medio:

| Paso | Dónde | Anotación | Qué dice |
|---|---|---|---|
| 1 | `ComposeAcademyApp` | `@HiltAndroidApp` | "acá empieza el grafo" |
| 2 | `MainActivity` | `@AndroidEntryPoint` | "esta Activity puede pedir dependencias" |
| 3 | casos de uso (`:core:domain`) | `@Inject constructor` | "sé construirme solo si me dan un repositorio" |
| 4 | `TodoRepositoryImpl`, `BookRepositoryImpl`, `InMemoryTodoDao` | `@Inject constructor` | ídem |
| 5 | `DataModule`, `DatabaseModule` | `@Binds` | "para esta interfaz, usá esta clase" |
| 6 | `NetworkModule` | `@Provides` | "para construir esto hay que ejecutar código" |
| 7 | los 4 ViewModels | `@HiltViewModel` | "el grafo sabe construir este ViewModel" |

**`@Binds` vs `@Provides`** es la pregunta que siempre aparece:

- `@Binds` → la clase es **nuestra** y ya tiene `@Inject constructor`. Solo falta decir qué interfaz cumple.
  Es más barato: Dagger no genera una fábrica, solo un alias. Va en un módulo `abstract`.
- `@Provides` → el objeto lo construye **otro** (Retrofit, OkHttp, Room). Hay que escribir el `return`.
  Va en un módulo `object`.

### 1.3 Dónde vive cada módulo (lo más importante de la clase)

```
:core:domain    javax.inject únicamente  → el dominio NO conoce Hilt ni Android
:core:network   NetworkModule   (@Provides BookApi, TodoApi)
:core:database  DatabaseModule  (@Binds TodoDao)
:core:data      DataModule      (@Binds TodoRepository, BookRepository)
:features:*     solo @HiltViewModel
:app            AppModule       (solo el flag @IsDebug)
```

La regla: **cada capa declara cómo se construye lo que ella implementa**. Lo contrario —un archivo
gigante de módulos en `:app`— es `AppContainer` otra vez, con anotaciones.

Hay que explicar dos cosas que llaman la atención:

- **`:core:domain` solo usa `javax.inject`** (`@Inject`, `@Qualifier`): son anotaciones estándar de Java,
  no Hilt. El dominio dice "necesito un `TodoRepository`" sin saber quién se lo va a dar. Si mañana el
  equipo migra a Koin o a Dagger puro, el dominio no cambia.
- **`:core:network`, `:core:database` y `:core:data` dejaron de ser módulos Kotlin puro y pasaron a ser
  `android.library`**, porque Hilt es Android-only. Es un costo real de usar Hilt y hay que nombrarlo.
  El dominio, que es la capa que más importa proteger, se mantiene puro.

### 1.4 Scopes: `@Singleton` no es decoración

Mostrar `DatabaseModule` y sacarle el `@Singleton` en vivo:

```kotlin
@Binds @Singleton abstract fun bindTodoDao(impl: InMemoryTodoDao): TodoDao
```

Sin `@Singleton`, Hilt crea un `InMemoryTodoDao` nuevo por cada inyección: cada pantalla tendría su
propia "base de datos" vacía. La app compila, arranca y se comporta mal **en silencio**. Es el bug
más típico al empezar con DI.

Pregunta para el curso: ¿y si ponemos `@Singleton` en todo? (se gasta memoria en objetos que podrían
morir con la pantalla; y un objeto con estado compartido sin querer es peor que crear uno nuevo).

### 1.5 Argumentos de navegación: assisted injection

Es el punto técnico más fino. `TodoDetailViewModel` necesita dos cosas de orígenes distintos:

- `getTodo`, `updateTodo` → los sabe el **grafo**
- `todoId` → lo sabe la **navegación** (lo eligió el usuario al tocar una tarea)

Solución (la receta oficial de Navigation 3):

```kotlin
@HiltViewModel(assistedFactory = TodoDetailViewModel.Factory::class)
class TodoDetailViewModel @AssistedInject constructor(
    @Assisted private val todoId: Int,
    private val getTodo: GetTodoUseCase,
    private val updateTodo: UpdateTodoUseCase
) : ViewModel() {
    @AssistedFactory interface Factory { fun create(todoId: Int): TodoDetailViewModel }
}
```

Y en `AppNavDisplay`:

```kotlin
entry<TodoDetailDestination> { key ->
    val viewModel = hiltViewModel<TodoDetailViewModel, TodoDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(key.todoId) }
    )
    TodoDetailRoute(viewModel = viewModel, onBack = { navigationState.goBackFrom(key) })
}
```

Dos detalles para remarcar:

1. Se inyecta el **`Int`**, no la `NavKey`: `TodoDetailDestination` vive en `:app` y la feature no la
   conoce. La regla de dependencias sigue intacta.
2. No hace falta pasar un `key` string para que cada tarea tenga su propio ViewModel: el
   `rememberViewModelStoreNavEntryDecorator()` que ya estaba desde la clase 1 le da a cada entrada del
   back stack su propio `ViewModelStore`.

### 1.6 El resultado

```kotlin
// Antes
AppShell(container) → AppNavDisplay(container) → viewModel { TodoListViewModel(container.observeTodos, …) }

// Después
AppShell() → AppNavDisplay() → hiltViewModel()
```

`AppContainer.kt` se borró. Mostrar el diff de `AppNavDisplay.kt` completo: es la foto más clara de la clase.

### 1.7 Errores típicos (provocarlos a propósito si hay tiempo)

| Error | Causa |
|---|---|
| `MissingBinding: TodoRepository cannot be provided` | falta el `@Binds` o el módulo no tiene `@InstallIn` |
| `@AndroidEntryPoint ... must be attached to an @HiltAndroidApp` | falta `@HiltAndroidApp` en la Application |
| La lista no se actualiza / cada pantalla ve datos distintos | falta `@Singleton` |
| `Cannot create an instance of class ...ViewModel` | falta `@HiltViewModel`, o se usó `viewModel()` en vez de `hiltViewModel()` |
| `DaggerHiltTestApplication ... not found` en tests | falta el `testInstrumentationRunner` custom |

---

## Parte 2 — Theming: Material 3 y un design system propio (~35 min)

### 2.1 El "antes"

`Color.kt` de la clase 1 era literalmente la plantilla de Android Studio:

```kotlin
val Purple80 = Color(0xFFD0BCFF)
val Purple40 = Color(0xFF6650a4)
```

Y en `PlaygroundScreen` había un `Color.Green` hardcodeado y un `Typography.headlineSmall` importado
del objeto en vez de leerlo del theme. Son los dos vicios a corregir.

### 2.2 Los cuatro conceptos

**1. Paleta tonal.** En M3 no se eligen colores sueltos: se parte de un color semilla y se genera una
rampa de tonos (0 = negro, 100 = blanco) para cada familia. El número en `Teal40` es el tono.
Herramienta: [Material Theme Builder](https://m3.material.io/theme-builder).

**2. Roles.** Los tonos no se usan en las pantallas: se asignan a roles en `Theme.kt`.
Los roles vienen **de a pares** (`primary` / `onPrimary`): si siempre se usan juntos, el contraste
está garantizado en claro y en oscuro. Los `container` son la versión suave del rol.

Mostrar también que el modo oscuro **no es el claro invertido**: usa `Teal80` (claro) sobre fondos
oscuros, porque un color saturado sobre negro pierde contraste y cansa la vista.

**3. La escala de superficies.** Esto se puede mostrar en vivo con un error real que tuvimos al armar
la clase: con el esquema de color definido pero **sin** los roles `surfaceContainer*`, la barra de
navegación quedaba **lila** — los componentes caían en el violeta por defecto de Material. Por eso el
esquema define `surfaceDim`, `surfaceBright` y los cinco `surfaceContainer*`.

**4. Extender el theme.** Material no trae espaciados. `Dimens.kt`:

```kotlin
data class AppSpacing(val xs: Dp = 4.dp, val sm: Dp = 8.dp, val md: Dp = 16.dp, …)
val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

object AppTheme {
    val spacing: AppSpacing @Composable get() = LocalAppSpacing.current
    val colors: ColorScheme @Composable get() = MaterialTheme.colorScheme
    …
}
```

Uso: `Modifier.padding(AppTheme.spacing.md)`.

`staticCompositionLocalOf` (y no `compositionLocalOf`) porque el valor no cambia durante la vida de la
app: si cambiara, Compose tendría que recomponer todo el árbol.

### 2.3 LIVE CODING: crear un componente del design system

Es la parte práctica del bloque. Construir una etiqueta de estado usando todo lo anterior.
El código completo, paso a paso, está en el guion de slides; acá el resumen para dictar.

**Paso 1 — la versión ingenua.** Escribirla en vivo, sin comentarios:

```kotlin
@Composable
fun StatusBadge(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = Color(0xFF7D5260),
        modifier = Modifier
            .background(Color(0xFFFFD8E4), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
```

Funciona y se ve bien. Que el curso encuentre los problemas: ilegible en oscuro, ignora el
tamaño de fuente del sistema, esquina sin relación con el resto, números mágicos, y un color
de marca imposible de cambiar en un solo lugar.

**Paso 2 — pasarlo al design system:**

```kotlin
enum class StatusTone { Pending, Done, Failed }

@Composable
fun StatusBadge(text: String, tone: StatusTone, modifier: Modifier = Modifier) {
    val (container, content) = when (tone) {
        StatusTone.Pending -> AppTheme.colors.secondaryContainer to AppTheme.colors.onSecondaryContainer
        StatusTone.Done    -> AppTheme.colors.primaryContainer   to AppTheme.colors.onPrimaryContainer
        StatusTone.Failed  -> AppTheme.colors.errorContainer     to AppTheme.colors.onErrorContainer
    }
    Text(
        text = text,
        style = AppTheme.typography.labelLarge,
        color = content,
        modifier = modifier
            .background(container, AppTheme.shapes.small)
            .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xs)
    )
}
```

Lo que hay que señalar:
- `container` y `content` **van en pareja** (`X` / `onX`): el contraste queda resuelto solo.
- `tone` es **semántico**: quien usa el componente elige un significado, no un color.
- Ni un solo valor hardcodeado, y el componente quedó más corto que el anterior.

**Paso 3 — previews en claro y oscuro.** No se escribió ningún color para el modo oscuro y aun
así se ve bien. Pegar al lado la versión del paso 1 para ver cómo se rompe.

**Paso 4 — blindarlo con un snapshot test** (se retoma en el bloque 3, sección 3.8).

**Paso 5 — tocar un token.** Cambiar `AppSpacing.sm` de `8.dp` a `12.dp`: falla el snapshot del
badge y el de espaciados. Un cambio en el design system, y el test dice qué componentes afectó.

Frase de cierre del bloque: *un design system no es una carpeta de constantes; es que un cambio
en un lugar tenga un efecto conocido, visible y verificable en toda la app.*

### 2.4 Demo en vivo: el tab Playground

El Playground ahora es la **vitrina del design system**: roles de color, escala tipográfica, formas y
espaciados, con dos interruptores.

Cosas para hacer en vivo:

1. Tocar **Modo oscuro** → cambia todo el subárbol. Notar que la bottom bar **no** cambia: está afuera
   del `ComposeAcademyTheme` anidado. Un theme anidado pisa al de afuera solo para sus hijos.
2. Tocar **Dynamic color** → en Android 12+ el sistema genera la paleta desde el fondo de pantalla del
   usuario y **tapa por completo** la paleta de marca. Por eso `dynamicColor = false` por defecto.
   Debate rápido: ¿marca o personalización?
3. Cambiar un tono en `Color.kt` y recompilar: cambia toda la app sin tocar ninguna pantalla.

### 2.5 El tema XML también existe

`app/src/main/res/values-night/themes.xml` heredaba de `Theme.Material.Light` igual que el de día: al
abrir la app en modo oscuro había un destello blanco antes del primer frame de Compose. El tema XML es
el de la **ventana**, no el de Compose, y sigue importando.

---

## Parte 3 — Testing inicial (~45 min)

### 3.1 Por qué recién ahora

Es el orden natural: **la DI es lo que hace testeable al código**. Un ViewModel que crea sus propias
dependencias con `new` no se puede testear sin red ni base de datos.

### 3.2 La pirámide, en este proyecto

| Tipo | Dónde | Velocidad | Qué prueba |
|---|---|---|---|
| Unit (JVM) | `src/test` de cada módulo | ms | reglas de negocio, lógica del ViewModel |
| Snapshot (JVM) | `core/ui/src/test` | segundos | cómo se **ve** un componente |
| Instrumentado | `app/src/androidTest` | segundos + emulador | que el **grafo** completo funcione |
| E2E / interacción | — | lento | *clase siguiente* |

Idea para remarcar: **ninguno reemplaza a otro**. Unitario: la lógica. Snapshot: la apariencia.
De grafo: que las dependencias se puedan construir. Cada uno ve algo que los otros no.

### 3.3 Fakes en vez de mocks

El proyecto no usa MockK ni Mockito. Un fake se comporta como el objeto real (si agregás una tarea, la
lista la refleja); un mock solo responde lo que le dijeron. Los tests con fakes se leen mejor y no se
rompen cuando cambia el orden de las llamadas.

Antes el `FakeTodoRepository` estaba **duplicado** en `:core:domain` y en `:features:todos`. Ahora hay
un módulo nuevo:

```
:core:testing   MainDispatcherRule · FakeTodoRepository · FakeBookRepository · TestData
```

Se consume siempre como `testImplementation(project(":core:testing"))`, nunca como `implementation`:
así no viaja en el APK.

### 3.4 `MainDispatcherRule`

`viewModelScope` usa `Dispatchers.Main`, que en la JVM no existe. Antes cada test repetía
`setMain`/`resetMain` en `@Before`/`@After`; ahora es una regla de JUnit:

```kotlin
@get:Rule val mainDispatcherRule = MainDispatcherRule()
```

### 3.5 El dispatcher decide qué podés observar

Este es el mejor momento de la parte de testing, y salió de un test que **falló** al escribir la clase.

`TodoListViewModelTest` usa `UnconfinedTestDispatcher`: las corrutinas corren apenas se lanzan, así que
el test puede mirar el estado final directamente. Pero cuando quisimos verificar con Turbine que el
estado pasa por `Loading`:

```
expected:<Loading> but was:<Success(todos=[...])>
```

El `init` del ViewModel ya había terminado antes de que el test pudiera observar. Por eso
`TodoListViewModelFlowTest` es una clase aparte con `StandardTestDispatcher`, que **encola** el trabajo
y lo ejecuta cuando el test se suspende en `awaitItem()`.

| Dispatcher | Cuándo usarlo |
|---|---|
| `UnconfinedTestDispatcher` | mirar el estado **final** (la foto) |
| `StandardTestDispatcher` | verificar la **secuencia** de estados (la película) |

Y **Turbine** es la librería para lo segundo:

```kotlin
viewModel.uiState.test {
    assertEquals(TodoListUiState.Loading, awaitItem())
    val success = awaitItem() as TodoListUiState.Success
    cancelAndIgnoreRemainingEvents()
}
```

### 3.6 Qué testear de un ViewModel

Con `TodoDetailViewModelTest` como ejemplo: no se testean los getters, se testean las **reglas**.

- se carga la tarea correcta por id
- un id inexistente deja el estado en `Error(NotFound)`
- editar abre el formulario con los valores actuales
- un título en blanco **no** guarda
- guardar actualiza la tarea y cierra el formulario
- si falla el guardado, **no se pierde lo que el usuario escribió**

Notar que este ViewModel usa `@AssistedInject` y, aun así, el test lo construye con su constructor
normal: **la inyección de dependencias es para la app, no para los tests**.

### 3.7 Test de Hilt (instrumentado)

```bash
./gradlew :app:connectedDebugAndroidTest   # necesita emulador
```

Un unit test no puede verificar el grafo, porque en un unit test las dependencias las arma el propio
test. `DependencyGraphTest` comprueba tres cosas que solo se ven con la app armada de verdad:

1. Hilt resuelve la cadena completa (`GetBooksUseCase → BookRepository → BookApi`).
2. `TodoRepository` es realmente un singleton (`assertSame` sobre dos inyecciones).
3. Lo que escribe un caso de uso lo observa otro (los scopes están bien).

Y acá se ve el pago de toda la clase: `FakeNetworkModule`

```kotlin
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NetworkModule::class])
object FakeNetworkModule { … }
```

El test corre contra la app **real** —mismos repositorios, mismos casos de uso— pero sin internet.
**No se modificó una sola línea de producción para lograrlo.** Eso es imposible cuando cada clase
construye sus propias dependencias.

Las dos piezas de infraestructura que hay que mostrar: `HiltTestRunner` (arranca con
`HiltTestApplication` en vez de `ComposeAcademyApp`) y la línea en `app/build.gradle.kts` que lo
declara. Olvidarse de esa línea es el error número uno.

### 3.8 Snapshot tests: testear lo que se VE

**Preparación obligatoria antes de la clase:** correr `./gradlew :core:ui:verifyRoborazziDebug`
una vez. La primera corrida baja e instrumenta el runtime de Robolectric (~200 MB) y tarda
varios minutos; después queda cacheado. No lo hagas en vivo.

**El problema.** Volvé al bug de la barra de navegación lila (parte 2). Los 26 tests unitarios
pasaban, la app compilaba y arrancaba. Nada lo detectó. Hay una clase entera de errores que no
rompe nada: un color mal asignado, un padding que se desarma, un texto que se corta, un
contraste ilegible en modo oscuro.

**La idea.** Un snapshot test dibuja el composable, lo guarda como imagen y, en cada corrida,
compara contra una imagen de referencia guardada en el repo.

```kotlin
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xhdpi")
class StateContentsSnapshotTest {

    @Test
    fun errorContent_dark() = capture("error_dark", darkTheme = true) {
        ErrorContent(title = "Ups, algo salió mal", message = "…", onRetry = {})
    }
}
```

Tres anotaciones a explicar:
- `RobolectricTestRunner`: permite dibujar Android en la JVM. Sin emulador.
- `GraphicsMode.NATIVE`: habilita el renderizado real (sin esto la imagen sale en blanco).
- `qualifiers`: fija pantalla y densidad, para que la imagen sea igual en cualquier máquina.

**El flujo de trabajo** (es lo más importante del bloque):

```bash
./gradlew :core:ui:recordRoborazziDebug    # (re)genera las imágenes de referencia
./gradlew :core:ui:verifyRoborazziDebug    # falla si algo cambió visualmente
./gradlew :core:ui:compareRoborazziDebug   # genera imágenes con las diferencias marcadas
```

Las imágenes viven en `core/ui/src/test/snapshots/images` y **se commitean**: son la referencia.
Si el cambio visual es intencional, se regraban y se commitea la imagen nueva.

**La frase que cierra el tema:** en un pull request, el revisor *ve* la imagen vieja y la nueva
una al lado de la otra. No tiene que confiar en que "no toqué nada visual".

### 3.9 DEMO EN VIVO: romper un componente

Guía paso a paso (fuera del repo): `~/Desktop/demo-snapshot-fail.md`, con su `.patch`.

Resumen: se cambia **una palabra** en `MessageContent` —el ícono de error pasa de
`colorScheme.error` a `colorScheme.onSurfaceVariant`—, se corren primero los tests unitarios
(**todos en verde**) y recién después los snapshots (**rojo**). Se abre la imagen de
comparación y se ve el triángulo gris.

La pregunta para el curso, entre un paso y el otro: *¿la app está bien?*

### 3.10 El costo, dicho en voz alta

Esto no es gratis y conviene que los alumnos lo escuchen:

| Costo | Detalle |
|---|---|
| Arranque | ~200 MB la primera vez en cada máquina |
| Compatibilidad | Hicieron falta flags `--add-exports`/`--add-opens` para el JDK 21, y fijar `@Config(sdk = [34])` porque Espresso falla contra Android 17 |
| Mantenimiento | Todo cambio visual intencional obliga a regrabar y commitear imágenes |
| Peso del repo | Son binarios |

Por eso están **solo** en los componentes compartidos de `:core:ui` y no en todas las pantallas:
se empieza por lo que usan todas las features y menos cambia.

---

## Verificación final

```bash
./gradlew assembleDebug
./gradlew test                             # unitarios + snapshots, sin emulador
./gradlew lintDebug
./gradlew :core:ui:verifyRoborazziDebug    # regresión visual
./gradlew :app:connectedDebugAndroidTest   # 3 tests de Hilt, con emulador
```

En el emulador: abrir los 3 tabs, entrar al detalle de una tarea y de un libro (verifica el assisted
injection), rotar la pantalla en el detalle (el ViewModel sobrevive y no recarga), y jugar con los dos
interruptores del Playground.

---

## Ejercicios para los alumnos

1. **Fácil.** Agregar un caso de uso nuevo (`CountPendingTodosUseCase`) y usarlo en `TodoListViewModel`.
   ¿Cuántos archivos hay que tocar para que Hilt lo inyecte? (uno: el caso de uso)
2. **Fácil.** Sacarle el `@Singleton` a `bindTodoDao` y explicar qué se rompe y por qué no falla el build.
3. **Medio.** Cambiar el color semilla de la paleta con Material Theme Builder y reemplazar `Color.kt`.
4. **Medio.** Agregar un token propio al design system (por ejemplo `AppElevation`) siguiendo el patrón
   de `AppSpacing`.
5. **Medio.** Escribir `BookDetailViewModelTest` usando `FakeBookRepository` (ya existe en `:core:testing`).
7. *(Difícil)* Hacer que `FakeTodoApi` se instale solo en debug y que en release se use una API real.
   Pista: `@Provides` distintos por variante de build, o un qualifier.

## Para la próxima

- Tests de **interacción** con Compose (`onNodeWithText`, `performClick`): hoy probamos cómo se ve un componente, falta probar qué pasa cuando el usuario lo toca
- E2E completos
- Llevar los snapshots a las pantallas completas de `:features:*`
- Reemplazar `InMemoryTodoDao` por Room real (ahora es cambiar un `@Binds`)
