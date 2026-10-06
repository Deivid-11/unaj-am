# Cómo publicar una clase (guía para el docente)

El proyecto crece clase a clase. Para que cada alumno pueda ver **cómo estaba el código al final de cada clase**,
usamos dos conceptos de git: *commit* y *tag*.

## Conceptos

- **Commit**: una "foto" del proyecto en un momento, con un mensaje que explica qué cambió.
- **Tag**: una **etiqueta con nombre** que se pega sobre un commit y no se mueve nunca. Es como poner un marcador
  en el libro: `clase-01-ui-mvvm` apunta siempre al código tal como quedó al terminar la clase 1,
  aunque después se sigan agregando commits para la clase 2, 3, etc.

Gracias al tag, un alumno que se perdió una clase puede volver a ese punto exacto:
```bash
git checkout clase-01-ui-mvvm   # ve el proyecto como estaba al final de la clase 1
git checkout main               # vuelve a la versión más nueva
```
(Si tu rama principal no se llama `main`, usá su nombre.)

## Qué es el Playground

El tab **Playground** es una pantalla libre para escribir código en vivo con los alumnos, sin tocar Tareas ni Libros.
Todo lo que se escribe va en `PlaygroundContent`, en
`features/playground/src/main/kotlin/ar/edu/unaj/am/myapplication/features/playground/PlaygroundScreen.kt`.

Ese código queda guardado en el archivo, así que **al terminar la clase hay que decidir qué hacer con él**:

| Opción | Cuándo conviene |
|---|---|
| **Dejarlo** | Si el ejemplo de la clase sirve como material para que los alumnos lo repasen. |
| **Dejar `PlaygroundContent` vacío** (un `Box` sin contenido) | Si querés que la clase siguiente arranque con la pantalla limpia. |

No es obligatorio ninguna de las dos: es una decisión didáctica. Lo importante es **hacerla antes de crear el tag**,
porque el tag congela lo que haya en ese momento.

## Pasos al terminar una clase

1. Dejá el Playground como quieras que lo vean los alumnos (ver arriba).
2. Verificá que todo compile y los tests pasen:
   ```bash
   ./gradlew assembleDebug test
   ```
3. Hacé el commit:
   ```bash
   git add .
   git commit -m "Clase 1: UI en Compose + MVVM"
   ```
4. Poné el tag sobre ese commit y subilo (los tags **no** se suben solos con un `git push` común):
   ```bash
   git tag clase-01-ui-mvvm
   git push
   git push origin clase-01-ui-mvvm
   ```

Convención de nombres: `clase-NN-tema` (ver la [hoja de ruta](../README.md#hoja-de-ruta)).

## Si te equivocaste con un tag

Un tag no se mueve solo. Para corregirlo hay que borrarlo y volver a crearlo:
```bash
git tag -d clase-01-ui-mvvm                    # lo borra en tu máquina
git push origin --delete clase-01-ui-mvvm      # lo borra en el repositorio remoto
git tag clase-01-ui-mvvm                       # lo crea de nuevo sobre el commit actual
git push origin clase-01-ui-mvvm
```
Si ya hay alumnos que lo descargaron, tendrán la versión vieja hasta que lo actualicen: conviene no mover tags ya publicados.
