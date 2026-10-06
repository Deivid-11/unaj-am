package ar.edu.unaj.am.myapplication.core.domain.model

/**
 * Modelo de dominio: representa el concepto de negocio "Tarea".
 *
 * No tiene anotaciones de Room, Moshi, Serialization ni nada de Android.
 * Si mañana cambiamos la API o la base de datos, este modelo no se toca.
 */
data class Todo(
    val id: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean
)
