# Arquitectura

Este documento describe **qué** hay en cada capa y qué puede depender de qué.
Para el **porqué** de cada decisión, sus costos y cuándo revisarla, ver [docs/decisiones.md](docs/decisiones.md).
Para correr el proyecto, ver el [README](README.md).

## Visión general

Tres tabs en una bottom bar:

| Tab | Fuente de datos | Estrategia |
|---|---|---|
| Tareas | `FakeTodoApi` + `InMemoryTodoDao` (simulados) | Offline-first: la base local es la fuente de verdad |
| Libros | [Gutendex](https://gutendex.com/books/) real con Retrofit | Red + caché en memoria |
| Playground | — | Pantalla libre para escribir ejemplos en vivo en clase |

Tareas y Libros usan exactamente el mismo patrón (UiState, ViewModel, casos de uso, repositorio):
solo cambia la implementación del repositorio en `:core:data`.

## Módulos y dependencias

```
                              :app
           (composition root: @HiltAndroidApp + navegación)
                 │          │            │
        :features:todos  :features:books  :features:playground
                 └────┬─────┘                   │
                      ▼                         │
                  :core:ui ◄────────────────────┘
                      │
                      ▼
                :core:domain  ◄──────  :core:data ──► :core:network
                      ▲                     │
                      └──── :core:database ◄┘

                :core:testing  (solo en testImplementation de los demás)
```

| Módulo | Puede depender de |
|---|---|
| `:app` | todos (es el único que los conecta) |
| `:features:*` | `:core:domain`, `:core:ui` |
| `:core:ui` | `:core:domain` |
| `:core:data` | `:core:domain`, `:core:network`, `:core:database` |
| `:core:database` | `:core:domain` |
| `:core:network`, `:core:domain` | nada |
| `:core:testing` | `:core:domain` (y solo entra como `testImplementation`) |

Las features **no ven** `:core:data`, `:core:network` ni `:core:database`: Gradle no las deja importar nada de ahí.

**Tipo de módulo.** `:core:domain` y `:core:testing` son Kotlin puro (`kotlin("jvm")`). `:core:data`,
`:core:network` y `:core:database` son `android.library` porque alojan sus propios módulos de Hilt,
que es una librería Android. El dominio se mantiene puro a propósito: es la capa que más conviene proteger.

## Layer Definitions

### App — `:app`

- `ComposeAcademyApp` (`@HiltAndroidApp`): arranca el grafo de dependencias. `MainActivity` es `@AndroidEntryPoint`
- `di/AppModule`: lo único que `:app` aporta al grafo, el flag `@IsDebug` (el resto lo declara cada capa)
- `navigation/`: **Navigation 3**. No hay `NavController`: el back stack es una lista de keys tipadas (`@Serializable … : NavKey`) que modificamos nosotros
  - `Destinations`: una key por pantalla; los argumentos son propiedades (`BookDetailDestination(bookId)`)
  - `NavigationState`: tab actual + un `NavBackStack` por tab; `navigate()` = `add`, `goBack()` = `removeAt(lastIndex)`
  - `AppNavDisplay`: `entryProvider { entry<BookDetailDestination> { key -> … key.bookId … } }` + `NavDisplay`
  - Cada entrada tiene su propio `ViewModelStore` (`rememberViewModelStoreNavEntryDecorator`), y los back stacks de todos los tabs quedan decorados, así que cambiar de tab no pierde ViewModels ni scroll
- `ui/AppShell`: `NavigationBar` armada desde `TopLevelDestination.entries`, oculta fuera de la pantalla principal de cada tab
- Es el único módulo que conoce todas las capas

### Presentation — `:features:*`

Un módulo por feature (una capacidad del usuario), con sus pantallas en paquetes `list/` y `detail/`:

- **`:features:todos`**: lista con pull-to-refresh, alta, completar y swipe para borrar; detalle con modo lectura/edición y validación
- **`:features:books`**: lista con scroll infinito (`derivedStateOf` + `LaunchedEffect`); detalle con portada (Coil), temas (`FlowRow`) y link externo (`LocalUriHandler`)
- **`:features:playground`**: pantalla libre para pruebas en vivo en clase (sin ViewModel ni dominio)

Convenciones de todas las features:
- Patrón **Route / Screen**: `XxxRoute` (con estado, conoce al ViewModel) → `XxxScreen` (sin estado, con `@Preview`)
- Las pantallas observan `StateFlow<UiState>` con `collectAsStateWithLifecycle()`
- Los ViewModels tienen un `MutableStateFlow<UiState>` privado y exponen un `StateFlow<UiState>` público
- `UiState` como `sealed interface` (`Loading` / `Success` / `Error`), con `when` exhaustivo
- Eventos de una sola vez guiados por estado (`isSaved`, `userMessage` + `onUserMessageShown()`), sin `Channel`
- Estado efímero de UI (diálogos, formulario de alta) en `rememberSaveable`, no en el ViewModel
- Los ViewModels reciben sus casos de uso por constructor, inyectados con `@HiltViewModel`
- Los de detalle usan `@AssistedInject` para recibir el id de navegación (`@Assisted todoId: Int`) junto con sus casos de uso
- Sin dependencia de `:core:data`, `:core:network` ni `:core:database`

### Shared UI — `:core:ui`

- `ComposeAcademyTheme`: esquema de color propio (claro y oscuro) con roles M3 completos, tipografía, shapes y `dynamicColor` apagado por defecto
- Design system propio: `AppSpacing` + `LocalAppSpacing` + el objeto `AppTheme` (`AppTheme.spacing.md`, `AppTheme.colors.primary`)
- `LoadingContent`, `ErrorContent` (con slot `extraAction`), `MessageContent`
- Sus **snapshot tests** viven acá mismo (`src/test`), y las imágenes de referencia en `src/test/snapshots/images`
- `DomainError.asMessage()`: traduce errores de dominio a string resources

### Domain — `:core:domain`

- Módulo Kotlin puro: sin Android ni frameworks
- Casos de uso de una sola responsabilidad con `operator fun invoke()`, algunos con reglas de negocio (validación del título, pendientes primero)
- `TodoRepository` y `BookRepository` son interfaces; las implementaciones viven en `:core:data`
- Los casos de uso llevan `@Inject constructor`, pero con `javax.inject`: anotaciones estándar, sin Hilt ni Android
- `AppResult<T>` + `DomainError`: los errores viajan como valores, no como excepciones
- Modelos de dominio (`Todo`, `Book`) sin anotaciones ni dependencias de librerías

### Data — `:core:data`

- `TodoRepositoryImpl`: **offline-first**. El DAO es la única fuente de verdad y la red actualiza el caché
- `BookRepositoryImpl`: red + caché en memoria (el detalle reutiliza el libro que ya trajo la lista)
- Mappers DTO → dominio / entidad, y `NetworkResult` → `AppResult` (por ejemplo `"Austen, Jane"` → `"Jane Austen"`)
- `di/DataModule`: `@Binds` de cada contrato del dominio a su implementación. Es el único lugar de la app donde se nombran los `…Impl`

### Network — `:core:network`

- `BookApi`: Retrofit real contra Gutendex, `BookDto` con `@Serializable` / `@SerialName`
- `NetworkFactory`: OkHttp (timeouts, logger inyectable) + converter kotlinx.serialization. El logger lo provee `:app` (tag `API_BOOKS`, solo en debug)
- `TodoApi` + `FakeTodoApi`: API simulada con latencia. La tarea con id `99` fuerza un error de red para mostrar el estado `Error`
- `NetworkResult<T>` (`HttpError`, `NetworkError`, `ParseError`) y `safeApiCall {}`
- `di/NetworkModule`: `@Provides` de `BookApi` y `TodoApi`. El flag `@IsDebug` (qualifier) lo provee `:app`, que es quien conoce `BuildConfig`

### Database — `:core:database` (simulado)

- `TodoDao` (interfaz, como un `@Dao` de Room) + `InMemoryTodoDao`, que expone un `Flow` reactivo
- `TodoEntity` + `TodoEntityMapper` (entidad → dominio)
- `di/DatabaseModule`: `@Binds` de `InMemoryTodoDao` → `TodoDao`, con `@Singleton` (si no, cada pantalla tendría su propia "base")

### Testing — `:core:testing`

Módulo Kotlin puro con lo que comparten los tests. Sus clases viven en `main` (no en `test`) para que
otros módulos puedan usarlas, y se consume siempre como `testImplementation`, así no viaja en el APK:

- `MainDispatcherRule`: regla de JUnit que reemplaza `Dispatchers.Main` por uno de test
- `FakeTodoRepository`, `FakeBookRepository`: dobles de prueba con comportamiento real
- `TestData`: builders (`todo()`, `book()`, `author()`) con valores por defecto

## Tests

```bash
./gradlew test                             # unitarios + snapshots, en la JVM
./gradlew :app:connectedDebugAndroidTest   # instrumentados, con emulador
```

| Módulo | Test | Qué verifica |
|---|---|---|
| `:core:domain` | `TodoUseCasesTest` | validación, orden, toggle |
| `:core:data` | `BookMapperTest`, `BookRepositoryImplTest` | mappers y caché |
| `:features:todos` | `TodoListViewModelTest` | carga, error, alta reactiva, refresh fallido |
| `:features:todos` | `TodoListViewModelFlowTest` | la **secuencia** de estados (`Loading → Success`) con Turbine |
| `:features:todos` | `TodoDetailViewModelTest` | formulario: edición, título en blanco, guardado y error al guardar |
| `:features:books` | `BookListViewModelTest` | paginación, última página, error al cargar más |
| `:app` (androidTest) | `DependencyGraphTest` | que Hilt resuelva el grafo completo y que los scopes sean correctos |
| `:core:ui` | `StateContentsSnapshotTest` | cómo se **ven** los componentes compartidos, en claro y en oscuro |

### Snapshot tests

Verifican lo que ningún assert puede: un color mal asignado, un padding roto, un contraste
ilegible en modo oscuro. Dibujan el composable, lo guardan como imagen y comparan contra una
referencia versionada en el repo.

```bash
./gradlew :core:ui:verifyRoborazziDebug    # falla si cambió algo visualmente
./gradlew :core:ui:recordRoborazziDebug    # regraba las referencias (cambio intencional)
./gradlew :core:ui:compareRoborazziDebug   # imágenes con las diferencias marcadas
```

- Herramienta: **Roborazzi** sobre Robolectric → corren en la JVM, sin emulador.
- Las imágenes de `core/ui/src/test/snapshots/images` **se commitean**: son la referencia, y el
  diff de un PR muestra el cambio visual.
- `@Config(sdk = [34])`: Robolectric simula Android 14, no la `compileSdk` del proyecto (37),
  porque Espresso —que Roborazzi usa por dentro— todavía no soporta los runtimes más nuevos.
- El módulo necesita flags `--add-exports` / `--add-opens` en los tests: Robolectric accede a
  internals de la JVM que están cerrados desde el JDK 17.

Tres criterios que conviene conocer antes de escribir un test acá:

- **Fakes, no mocks.** Un fake se comporta como el objeto real; los tests se leen mejor y no se rompen cuando cambia el orden de las llamadas.
- **El dispatcher decide qué se puede observar.** `UnconfinedTestDispatcher` ejecuta todo al instante y sirve para mirar el estado final; `StandardTestDispatcher` encola el trabajo y es el único que permite ver los estados intermedios (por eso `TodoListViewModelFlowTest` es una clase aparte).
- **Cada tipo de test cubre algo que los otros no pueden.** Unitario: la lógica. De grafo: que las dependencias se puedan construir. De snapshot: cómo se ve. Ninguno reemplaza a otro.

## Próximos pasos

Ver la [hoja de ruta en el README](README.md#hoja-de-ruta) y las decisiones que dejamos afuera a propósito en
[docs/decisiones.md](docs/decisiones.md#lo-que-dejamos-afuera-a-propósito).
