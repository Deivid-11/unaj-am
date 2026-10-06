package ar.edu.unaj.am.myapplication.core.network.model

/**
 * DTO (Data Transfer Object): la forma EXACTA en la que el servidor manda los datos.
 *
 * Con Retrofit + Moshi acá irían anotaciones como @Json(name = "completed").
 * Fijate que el campo se llama `completed` y no `isCompleted`: el servidor
 * no tiene por qué hablar el mismo idioma que nuestro dominio. Por eso existen los mappers.
 */
data class TodoDto(
    val id: Int,
    val title: String,
    val description: String,
    val completed: Boolean
)
