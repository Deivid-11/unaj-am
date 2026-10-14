package ar.edu.unaj.am.myapplication.core.network.di

import android.util.Log
import ar.edu.unaj.am.myapplication.core.network.BookApi
import ar.edu.unaj.am.myapplication.core.network.NetworkFactory
import ar.edu.unaj.am.myapplication.core.network.TodoApi
import ar.edu.unaj.am.myapplication.core.network.fake.FakeTodoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt de la capa de red.
 *
 * Se usa @Provides (y no @Binds) porque estos objetos NO los construimos nosotros
 * con un constructor anotado: los arma Retrofit. Regla práctica:
 *
 * - @Binds   -> "para esta interfaz, usá esta clase mía" (más barato, sin código extra).
 * - @Provides -> "para construir esto hay que ejecutar código" (librerías de terceros).
 *
 * @InstallIn(SingletonComponent::class): estas dependencias viven en el componente
 * que dura lo mismo que la Application.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val API_BOOKS_TAG = "API_BOOKS"

    /**
     * Un solo cliente HTTP para toda la app: crear Retrofit es caro y OkHttp
     * reutiliza el pool de conexiones. Por eso @Singleton.
     *
     * En Logcat: filtrar por tag:API_BOOKS. Solo en debug; en release no se loguea nada.
     */
    @Provides
    @Singleton
    fun provideBookApi(@IsDebug isDebug: Boolean): BookApi =
        NetworkFactory.createBookApi(
            logger = if (isDebug) { message -> Log.d(API_BOOKS_TAG, message) } else null
        )

    /**
     * Hoy devolvemos el "servidor" falso. El día que exista una API real de tareas,
     * esta es la ÚNICA línea que cambia en toda la app: ni los repositorios,
     * ni los casos de uso, ni los ViewModels se enteran.
     */
    @Provides
    @Singleton
    fun provideTodoApi(): TodoApi = FakeTodoApi()
}
