plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // Procesador de anotaciones (genera el código de Hilt) y plugin de Hilt.
    // Se declaran acá con `apply false` y cada módulo aplica solo los que necesita.
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    // Snapshot testing de composables (tareas record/verify/compare)
    alias(libs.plugins.roborazzi) apply false
}
