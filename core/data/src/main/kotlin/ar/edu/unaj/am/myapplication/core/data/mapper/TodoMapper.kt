package ar.edu.unaj.am.myapplication.core.data.mapper

import ar.edu.unaj.am.myapplication.core.database.model.TodoEntity
import ar.edu.unaj.am.myapplication.core.domain.model.Todo
import ar.edu.unaj.am.myapplication.core.network.model.TodoDto

// Cada capa tiene su propio modelo. Los mappers son la "aduana" entre ellas.

internal fun TodoDto.toDomain() = Todo(
    id = id,
    title = title,
    description = description,
    isCompleted = completed
)

internal fun TodoDto.toEntity() = TodoEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = completed
)

internal fun Todo.toDto() = TodoDto(
    id = id,
    title = title,
    description = description,
    completed = isCompleted
)
