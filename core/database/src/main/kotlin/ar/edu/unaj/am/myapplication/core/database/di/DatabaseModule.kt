package ar.edu.unaj.am.myapplication.core.database.di

import ar.edu.unaj.am.myapplication.core.database.dao.InMemoryTodoDao
import ar.edu.unaj.am.myapplication.core.database.dao.TodoDao
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt de la capa de persistencia.
 *
 * @Binds: la clase ya sabe construirse sola (tiene @Inject constructor), solo hay que
 * decirle a Hilt qué interfaz cumple. Por eso el módulo es `abstract` y el método también.
 *
 * @Singleton es OBLIGATORIO acá: nuestra "base de datos" guarda las tareas en memoria.
 * Si Hilt creara un InMemoryTodoDao nuevo por cada inyección, cada pantalla tendría
 * su propia tabla vacía. Probalo en clase sacando la anotación.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindTodoDao(impl: InMemoryTodoDao): TodoDao
}
