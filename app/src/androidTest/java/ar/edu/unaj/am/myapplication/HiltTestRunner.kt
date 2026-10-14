package ar.edu.unaj.am.myapplication

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * En un test instrumentado no queremos arrancar ComposeAcademyApp (la app de verdad),
 * sino HiltTestApplication: una Application que arma el grafo permitiendo REEMPLAZAR
 * módulos por otros de prueba.
 *
 * Este runner hace ese cambio. Se declara en app/build.gradle.kts:
 *
 *     testInstrumentationRunner = "ar.edu.unaj.am.myapplication.HiltTestRunner"
 *
 * Olvidarse de esta línea es el error más típico al empezar con tests de Hilt.
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?
    ): Application = super.newApplication(classLoader, HiltTestApplication::class.java.name, context)
}
