package ar.edu.unaj.am.myapplication.core.database.mapper

import ar.edu.unaj.am.myapplication.core.database.model.TodoEntity
import ar.edu.unaj.am.myapplication.core.domain.model.Todo

fun TodoEntity.toDomain() = Todo(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted
)
