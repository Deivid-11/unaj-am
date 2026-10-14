package ar.edu.unaj.am.myapplication.core.network.di

import javax.inject.Qualifier

/**
 * Un QUALIFIER distingue dos dependencias del mismo tipo.
 *
 * Sin esto, pedir un `Boolean` sería ambiguo: ¿cuál de todos los booleanos de la app?
 * Con `@IsDebug` le decimos a Hilt exactamente cuál queremos.
 *
 * Lo necesitamos porque `BuildConfig.DEBUG` vive en :app, y este módulo no lo conoce:
 * :app lo provee (ver AppModule) y acá solo lo consumimos.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IsDebug
