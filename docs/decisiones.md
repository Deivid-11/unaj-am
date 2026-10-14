# Decisiones de arquitectura

Para cada decisión: **qué** hicimos, **por qué**, **qué nos cuesta** y **cuándo conviene revisarla**.
La arquitectura no es gratis: todo lo de acá tiene un costo, y es parte de la lección saber cuándo vale la pena.

Índice: [1 Modularizar](#1-modularizar-desde-el-día-1) · [2 Regla de dependencias](#2-regla-de-dependencias) ·
[3 Un módulo por feature](#3-un-módulo-por-feature) · [4 Dominio en Kotlin puro](#4-dominio-en-kotlin-puro) ·
[5 Repositorio como interfaz](#5-el-repositorio-es-una-interfaz) · [6 Casos de uso](#6-casos-de-uso) ·
[7 Un modelo por capa](#7-un-modelo-por-capa-y-mappers) · [8 Errores como valores](#8-errores-como-valores) ·
[9 Hilt](#9-hilt-y-dónde-vive-cada-módulo) · [10 Route y Screen](#10-route-y-screen) ·
[11 UiState y eventos](#11-uistate-sealed-y-eventos-como-estado) · [12 Estrategia de datos](#12-estrategia-de-datos-por-feature) ·
[13 Navigation 3](#13-navigation-3) · [14 Datos simulados](#14-datos-simulados) ·
[15 Assisted injection](#15-assisted-injection-para-los-argumentos-de-navegación) ·
[16 Design system](#16-design-system-propio-sobre-material-3) ·
[17 Fakes en vez de mocks](#17-fakes-en-vez-de-mocks) ·
[18 Dynamic color apagado](#18-dynamic-color-apagado-por-defecto) ·
[19 Snapshot testing](#19-snapshot-testing-con-roborazzi)

---

## 1. Modularizar desde el día 1

**Qué:** el proyecto tiene 10 módulos de Gradle en vez de un solo `app`.

**Por qué:**
- Es mucho más barato separar al principio que desarmar un monolito después.
- Gradle **impide** violar la arquitectura: si `:features:todos` no declara `:core:data` como dependencia, no puede importar nada de ahí. En un solo módulo, la regla es una promesa; en varios, el compilador la hace cumplir.
- Los módulos independientes compilan en paralelo y se recompilan solo cuando cambian.

**Cuesta:** más archivos de configuración (los `build.gradle.kts` de los módulos Android son casi idénticos, y con Hilt la repetición creció: varios módulos declaran los mismos dos plugins y las mismas dos dependencias) y más cosas para navegar.

**Revisar:** si el proyecto fuera de una sola pantalla y una sola persona, un módulo alcanzaría. Los *convention plugins* reducen la repetición de configuración.

## 2. Regla de dependencias

**Qué:** las capas externas dependen de las internas, nunca al revés.

| Módulo | Puede depender de |
|---|---|
| `:app` | todos (es el que los conecta) |
| `:features:*` | `:core:domain`, `:core:ui` |
| `:core:ui` | `:core:domain` |
| `:core:data` | `:core:domain`, `:core:network`, `:core:database` |
| `:core:database` | `:core:domain` |
| `:core:network`, `:core:domain` | nada |
| `:core:testing` | `:core:domain` (y entra en los demás solo como `testImplementation`) |

**Por qué:** lo que más cambia (UI, librerías de red, base de datos) queda afuera; lo que menos cambia (las reglas del negocio) queda en el centro y no se entera de nada de lo de afuera.

## 3. Un módulo por feature

**Qué:** cada feature es **una capacidad del usuario** (Tareas, Libros) y contiene sus pantallas en paquetes: `list/` y `detail/`.

**Por qué:** Clean Architecture habla de capas y no dice cuántas pantallas van en un módulo; eso es una decisión de **cohesión**: lo que cambia junto, vive junto. La lista y el detalle de una tarea comparten modelo, flujo y navegación, y se modifican juntos. La documentación de Android respalda esto:
- Define un feature como *"an isolated part of an app's functionality that usually corresponds to a screen or series of closely related screens"* ([patrones de modularización](https://developer.android.com/topic/modularization/patterns)).
- Aconseja: *"If two modules heavily rely on knowledge of each other, it may be a good sign that they should actually act as one system"* (misma página).
- Advierte que un módulo demasiado fino (*"too fine-grained"*) trae *"increased build complexity and boilerplate code"* ([modularización, sección "Common pitfalls"](https://developer.android.com/topic/modularization)). Y el extremo opuesto también es un problema: módulos demasiado grandes terminan siendo otro monolito.

**Cuándo separar un detalle en su propio módulo** (tiene que haber una razón que se pueda nombrar):
1. Se abre desde **varios lugares** (por ejemplo, el detalle de un libro desde Libros, Favoritos y Búsqueda).
2. Lo mantienen **equipos distintos**.
3. Hay un motivo medible de **tiempo de build** o de carga opcional.

Hoy ninguna se cumple, por eso no están separados. Cuando aparezca el segundo punto de entrada, se extrae: es barato porque cada pantalla ya es `Route`/`Screen` y navega por callbacks.

## 4. Dominio en Kotlin puro

**Qué:** `:core:domain` es un módulo JVM (`kotlin("jvm")`): sin Android, sin Compose, sin Room, sin Retrofit.

**Por qué:** no *puede* importar nada de Android porque no lo tiene en el classpath. Sus tests corren en la JVM, en milisegundos, sin emulador.

**Cuesta:** los modelos de dominio no pueden llevar anotaciones de librerías; por eso existen los mappers (decisión 7).

## 5. El repositorio es una interfaz

**Qué:** `TodoRepository` y `BookRepository` (interfaces) viven en `:core:domain`; las implementaciones viven en `:core:data`.

**Por qué:** es la **inversión de dependencias**: el dominio define *qué* necesita y la capa de datos se adapta. Así `:core:data` depende de `:core:domain` y no al revés, y en los tests se reemplaza por un fake sin red ni base de datos.

**Cuesta:** una interfaz con una sola implementación parece de más. Se justifica porque ya tenemos **dos** estrategias distintas (decisión 12) y siempre una tercera en los tests.

## 6. Casos de uso

**Qué:** clases de una sola responsabilidad con `operator fun invoke()`.

**Por qué:** concentran las **reglas de negocio** fuera de la UI y de los datos. Ejemplos reales del proyecto: `AddTodoUseCase` valida el título y recorta espacios; `ObserveTodosUseCase` ordena las pendientes primero.

**Cuesta, y conviene decirlo:** varios casos de uso de este proyecto son un simple pasamanos al repositorio (`GetTodoUseCase`, `RefreshTodosUseCase`, `DeleteTodoUseCase`, `GetBooksUseCase`…). Es *boilerplate*. Muchos equipos omiten los triviales y llaman al repositorio directo desde el ViewModel; otros prefieren la consistencia de pasar siempre por un caso de uso. Acá elegimos la consistencia porque el objetivo es enseñar el patrón completo.

**Revisar:** si un caso de uso no tiene lógica y no la va a tener, es válido eliminarlo.

## 7. Un modelo por capa y mappers

**Qué:** el mismo concepto existe en tres formas: `TodoDto` (cómo lo manda el servidor), `TodoEntity` (cómo se guarda) y `Todo` (cómo lo entiende el negocio). Los mappers convierten entre ellas.

**Por qué:** cada capa evoluciona por separado. Si el servidor renombra `completed` a `done`, solo cambia el DTO y su mapper. El mapper también adapta formatos: `"Austen, Jane"` se convierte en `"Jane Austen"`.

**Cuesta:** más clases y más código repetitivo. Es el costo más visible de Clean Architecture.

## 8. Errores como valores

**Qué:** los repositorios y casos de uso devuelven `AppResult<T>` (`Success` / `Error(DomainError)`) en lugar de lanzar excepciones.

**Por qué:** una excepción es invisible en la firma de la función: nada obliga a manejarla. Con un tipo, el `when` del ViewModel es exhaustivo y el compilador exige tratar el error. Además la UI nunca ve un `IOException` ni un código HTTP: ve `DomainError.Network`, y decide qué texto mostrar.

## 9. Hilt, y dónde vive cada módulo

**Qué:** la inyección de dependencias la resuelve Hilt. Cada capa expone su propio módulo
(`NetworkModule`, `DatabaseModule`, `DataModule`) y `:app` solo aporta el flag `@IsDebug`.
En la clase 1 esto era `AppContainer`, un archivo que creaba todo a mano.

**Por qué Hilt:** el problema nunca fue crear objetos, fue **mantener a mano el grafo de quién necesita
a quién**. `AppContainer` crecía con cada feature, había que acordarse de reutilizar instancias para
compartir estado, y el `container` viajaba como parámetro por pantallas que no lo usaban. Hilt genera
ese mismo código y, sobre todo, hace explícito el **scope** (`@Singleton`), que antes era un `by lazy`
y una convención.

**Por qué los módulos están repartidos y no todos en `:app`:** porque cada capa declara cómo se
construye lo que ella implementa; es lo que recomienda la guía oficial y lo que hace *Now in Android*.
Juntarlos todos en `:app` sería `AppContainer` otra vez, con anotaciones.

**Por qué el dominio no conoce Hilt:** `:core:domain` usa solo `javax.inject` (`@Inject`, `@Qualifier`),
que son anotaciones estándar de Java. El dominio declara qué necesita sin saber quién se lo va a dar:
migrar a Koin o a Dagger puro no lo tocaría.

**Cuesta:**
- `:core:data`, `:core:network` y `:core:database` dejaron de ser módulos Kotlin puro y pasaron a
  `android.library`, porque Hilt es Android-only. Compilan un poco más lento. El dominio se mantuvo puro.
- Aparece código generado: los errores de Dagger son largos y, al principio, difíciles de leer.
- KSP agrega una tarea de build por módulo.

**Revisar:** en una app de una sola pantalla, la DI manual sigue siendo más clara que Hilt.

## 10. Route y Screen

**Qué:** cada pantalla se divide en `XxxRoute` (con estado, conoce al ViewModel) y `XxxScreen` (sin estado: recibe datos y lambdas).

**Por qué:** es *state hoisting*. `Screen` no depende del ViewModel, así que se ve en `@Preview`, se testea y se reutiliza. Los previews del proyecto (Success, Error, Loading, Empty) salen de esta separación.

## 11. UiState sealed y eventos como estado

**Qué:** el estado de una pantalla es una `sealed interface` (`Loading` / `Success` / `Error`) expuesta como `StateFlow`. Los eventos de una sola vez (un snackbar, volver atrás tras guardar) se modelan **como estado**: un campo (`userMessage`, `isSaved`) que la UI consume y avisa que consumió.

**Por qué:** la pantalla está en un solo estado a la vez y el `when` exhaustivo impide olvidar uno. Para los eventos, la [guía de Android sobre eventos de UI](https://developer.android.com/topic/architecture/ui-layer/events) indica que *"ViewModel events should always result in a UI state update"* y advierte que con `Channel` u otros streams, cuando el ViewModel vive más que la UI, *"these solutions don't guarantee the delivery and processing of those events"*. Con estado, en cambio, hay más garantías de entrega y es más fácil de testear.

**Cuesta:** requiere el paso extra de "consumir" el evento (`onUserMessageShown()`).

## 12. Estrategia de datos por feature

**Qué:** Tareas es **offline-first** (la base local es la única fuente de verdad y la red la actualiza); Libros es **red + caché en memoria** (la lista ya trae el libro completo, así que el detalle lo reutiliza).

**Por qué:** muestra que la estrategia de datos es una decisión del *repositorio*. Los ViewModels de las dos features son prácticamente iguales: no saben de dónde vienen los datos. El caché de Libros surgió de un problema real: el endpoint de detalle de Gutendex tardaba ~35 segundos.

## 13. Navigation 3

**Qué:** no hay `NavController`. El back stack es una lista de *keys* tipadas (`@Serializable … : NavKey`) y navegar es modificarla.

**Por qué:** es consistente con el resto de Compose: la UI es función del estado, y el back stack es un estado más. Los argumentos son propiedades tipadas (`key.bookId`), sin strings ni `getInt("id")`. Cada tab tiene su propio back stack y su propio `ViewModelStore`, así que cambiar de tab no pierde estado.

**Cuesta:** `NavigationState` (la lógica de tabs) lo escribimos nosotros: son unas 60 líneas visibles, en lugar de configuración interna de la librería.

## 14. Datos simulados

**Qué:** la API y la base de Tareas son falsas (`FakeTodoApi`, `InMemoryTodoDao`) detrás de las mismas interfaces que tendrían las reales (`TodoApi`, `TodoDao`).

**Por qué:** la primera clase era de UI y MVVM; conectar Room y un backend agregaba ruido. Pero la arquitectura ya está preparada: reemplazarlos es cambiar una línea de `NetworkModule` (la API) o un `@Binds` de `DatabaseModule` (la base). Libros sí usa Retrofit real, para mostrar cómo se ve.

**Ojo:** `FakeTodoApi` está en el `main` de `:core:network`, no en un módulo de tests, porque es la fuente de datos *de la app*, no de los tests. Los dobles de prueba sí viven aparte, en `:core:testing`.

## 15. Assisted injection para los argumentos de navegación

**Qué:** `TodoDetailViewModel` y `BookDetailViewModel` usan `@AssistedInject` + `@AssistedFactory`, y se
crean con `hiltViewModel(creationCallback = { it.create(key.todoId) })`.

**Por qué:** un ViewModel de detalle necesita dos cosas de orígenes distintos: sus casos de uso, que los
sabe el grafo, y el id, que lo sabe la navegación recién cuando el usuario toca un ítem. Assisted
injection combina las dos mitades y es la [receta oficial para Navigation 3](https://developer.android.com/guide/navigation/navigation-3/recipes/passingarguments).

**Por qué se inyecta el `Int` y no la `NavKey`:** `TodoDetailDestination` vive en `:app`, y las features
no conocen `:app`. Pasar la key rompería la regla de dependencias (decisión 2).

**Cuesta:** una interfaz `Factory` por cada ViewModel con argumentos.

**Alternativa descartada:** `SavedStateHandle`, que es lo habitual con Navigation 2, porque Navigation 3
no escribe los argumentos ahí: las keys son objetos tipados, no un `Bundle`.

## 16. Design system propio sobre Material 3

**Qué:** `ComposeAcademyTheme` define el esquema de color completo (incluidos los `surfaceContainer*`),
tipografía y shapes; y se extiende con tokens propios vía `AppSpacing` + `LocalAppSpacing`, accesibles
desde el objeto `AppTheme`.

**Por qué extender y no reemplazar:** Material trae colores, tipografías y formas, pero no espaciados.
Sin un token, los `16.dp` se desparraman y nadie sabe por qué una pantalla respira distinto que otra.
Un `CompositionLocal` es la forma estándar de sumar valores al árbol sin pasarlos por parámetro.

**Por qué definir los roles de superficie:** si el esquema no los define, los componentes de M3 caen en
el violeta por defecto de Material. Pasó en este proyecto: con la paleta propia ya aplicada, la barra de
navegación seguía lila.

**Cuesta:** un `Color.kt` largo (una rampa tonal por familia) que conviene generar con
[Material Theme Builder](https://m3.material.io/theme-builder) en lugar de escribir a mano.

## 17. Fakes en vez de mocks

**Qué:** no hay MockK ni Mockito. Los dobles de prueba son clases escritas a mano en `:core:testing`.

**Por qué:** un fake *se comporta* como el objeto real (si agregás una tarea, la lista la refleja);
un mock solo devuelve lo que le dijeron. Los tests con fakes se leen como el escenario que describen y
no se rompen cuando cambia el orden de las llamadas. Como los contratos son interfaces del dominio
(decisión 5), escribir el fake es barato.

**Cuesta:** mantener el fake cuando la interfaz cambia. Por eso hay **uno solo** por contrato,
compartido, en vez de uno por módulo de test.

**Revisar:** para interfaces de terceros, grandes y ajenas, un mock suele convenir.

## 18. Dynamic color apagado por defecto

**Qué:** `ComposeAcademyTheme(dynamicColor = false)` por defecto. En la clase 1 estaba en `true`.

**Por qué:** en Android 12+ el dynamic color genera la paleta desde el fondo de pantalla del usuario y
**tapa por completo** la paleta de marca. Para una app con identidad propia —y para una clase donde lo
que queremos ver es justamente nuestra paleta— el default razonable es apagarlo.

**Revisar:** es una decisión de producto, no técnica. El interruptor sigue estando, y el tab Playground
permite prenderlo en vivo para discutirlo.

## 19. Snapshot testing con Roborazzi

**Qué:** los componentes compartidos de `:core:ui` tienen tests que los dibujan, guardan el
resultado como PNG y lo comparan contra una imagen de referencia versionada en el repo
(`core/ui/src/test/snapshots/images`).

**Por qué:** hay una clase entera de errores que ningún assert detecta, porque no rompen nada:
un color mal asignado, un padding que se desarma, un texto que se corta, un contraste ilegible
en modo oscuro. La app compila, los tests pasan, y se ve mal. Pasó en este mismo proyecto:
con la paleta propia ya aplicada, la barra de navegación seguía saliendo lila porque faltaban
los roles `surfaceContainer*` (ver decisión 16). Un snapshot test lo habría atrapado solo.

**Por qué acá y no en `:features:*`:** empezamos por los componentes compartidos porque son los
que usan todas las pantallas y los que menos cambian. Extenderlo a las pantallas completas es
barato (ya son `Screen` sin estado), pero multiplica las imágenes a mantener.

**Por qué Roborazzi y no Paparazzi ni el plugin oficial:** era la única opción **estable** al
momento de la clase. Paparazzi está en `2.0.0-alpha05.1` y el plugin oficial de Google
(`com.android.compose.screenshot`) sigue en `0.0.1-alphaXX` y ya está deprecado a favor de los
*test suites* de AGP, que requieren una versión alpha de AGP.

**Cuesta, y bastante:**
- **Arranque lento.** La primera corrida en cada máquina baja e instrumenta el runtime de
  Robolectric (~200 MB). En este proyecto tardó varios minutos. Después queda cacheado en `~/.m2`.
- **Dos parches de compatibilidad.** Robolectric necesita flags `--add-exports`/`--add-opens`
  para acceder a internals de la JVM (cerrados desde el JDK 17), y hubo que fijar
  `@Config(sdk = [34])` porque Espresso —que Roborazzi usa internamente— falla contra el
  runtime de Android 17 con `NoSuchMethodException: InputManager.getInstance()`.
- **Mantenimiento.** Todo cambio visual intencional obliga a regrabar las imágenes y commitearlas.
- **Peso del repo.** Son binarios; crecen rápido si se agregan muchas pantallas y variantes.

**Revisar:** si la suite se vuelve lenta o las imágenes empiezan a regrabarse "porque sí" sin que
nadie mire el diff, el test dejó de aportar. También conviene revisar cuando Paparazzi o el
plugin oficial lleguen a estable.


---

## Lo que dejamos afuera a propósito

| Tema | Por qué no está todavía | Dónde encaja |
|---|---|---|
| Tests de UI con Compose y E2E | Primero los unitarios y el test del grafo | Clase 3 |
| Room real | Detrás de `TodoDao` ya está su lugar | A definir |
| Convention plugins | Primero sentir la repetición de los `build.gradle.kts` | A definir |
| Paging 3 | La paginación manual de Libros enseña la idea de fondo | A definir |
