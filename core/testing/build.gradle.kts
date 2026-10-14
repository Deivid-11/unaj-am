import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Módulo Kotlin puro con utilidades de test compartidas (fakes, reglas, datos de prueba).
// OJO: sus clases viven en `main` a propósito, para que OTROS módulos puedan usarlas
// desde sus tests. Nunca se agrega como `implementation`, siempre como `testImplementation`,
// así no termina dentro del APK.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    api(project(":core:domain"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.test)
}
