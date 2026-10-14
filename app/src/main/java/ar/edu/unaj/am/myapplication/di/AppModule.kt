package ar.edu.unaj.am.myapplication.di

import ar.edu.unaj.am.myapplication.BuildConfig
import ar.edu.unaj.am.myapplication.core.network.di.IsDebug
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Lo único que :app le aporta al grafo.
 *
 * Antes este paquete tenía AppContainer, un archivo que crecía con cada pantalla nueva
 * y donde había que acordarse de compartir instancias a mano. Ahora cada capa declara
 * sus propias dependencias (NetworkModule, DatabaseModule, DataModule) y :app solo
 * aporta lo que únicamente él sabe: en qué variante de build estamos.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IsDebug
    fun provideIsDebug(): Boolean = BuildConfig.DEBUG
}
