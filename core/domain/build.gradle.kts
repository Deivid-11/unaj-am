import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Módulo Kotlin puro: no conoce Android. Se compila y testea en la JVM.
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
    implementation(libs.kotlinx.coroutines.core)
    // Solo las ANOTACIONES de inyección (@Inject, @Qualifier), no una librería de DI.
    // El dominio declara "necesito un TodoRepository" sin saber quién se lo va a dar
    // ni con qué framework: por eso acá no hay Hilt, ni Android.
    // Es `api` para que los módulos que lo consumen vean las anotaciones.
    api(libs.javax.inject)

    // Fakes y reglas compartidas. Va solo en testImplementation: no viaja en el APK.
    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
