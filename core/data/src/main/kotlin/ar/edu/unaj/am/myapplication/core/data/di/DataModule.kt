package ar.edu.unaj.am.myapplication.core.data.di

import ar.edu.unaj.am.myapplication.core.data.repository.BookRepositoryImpl
import ar.edu.unaj.am.myapplication.core.data.repository.TodoRepositoryImpl
import ar.edu.unaj.am.myapplication.core.domain.repository.BookRepository
import ar.edu.unaj.am.myapplication.core.domain.repository.TodoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Acá se cierra el círculo de Clean Architecture: el dominio define el CONTRATO
 * (TodoRepository) y esta capa dice qué clase lo cumple (TodoRepositoryImpl).
 *
 * Nadie más en la app nombra las implementaciones: los casos de uso piden la interfaz
 * y Hilt resuelve el resto. Cambiar de fuente de datos = cambiar este archivo.
 *
 * @Singleton: BookRepositoryImpl tiene un caché en memoria y TodoRepositoryImpl
 * comparte el DAO entre lista y detalle. Una instancia por app.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindTodoRepository(impl: TodoRepositoryImpl): TodoRepository

    @Binds
    @Singleton
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository
}
