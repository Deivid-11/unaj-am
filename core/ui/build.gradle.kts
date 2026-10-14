plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    // Snapshot testing: agrega las tareas recordRoborazziDebug / verifyRoborazziDebug
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "ar.edu.unaj.am.myapplication.core.ui"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            // Robolectric necesita los recursos (colores, strings, dimens) para poder
            // dibujar de verdad en la JVM. Sin esto, los snapshots salen vacíos.
            isIncludeAndroidResources = true
        }
    }
}

// Robolectric necesita llegar a internals de la JVM para simular Android.
// Desde JDK 17+ eso está cerrado por defecto: sin estos flags, los tests fallan con
// "Failed to interact with raw FileDescriptor internals; perhaps JRE has changed?".
tasks.withType<Test>().configureEach {
    jvmArgs(
        "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED"
    )
}

dependencies {
    implementation(project(":core:domain"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // --- Snapshot testing (corre en la JVM, sin emulador) ---
    testImplementation(libs.junit)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
