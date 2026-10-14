plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    // Genera el código de serialización de las rutas de navegación type-safe (@Serializable)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "ar.edu.unaj.am.myapplication"
    compileSdk = 37

    defaultConfig {
        applicationId = "ar.edu.unaj.am.myapplication"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // Runner propio: los tests instrumentados necesitan arrancar con HiltTestApplication
        // en lugar de ComposeAcademyApp, para poder reemplazar módulos del grafo.
        testInstrumentationRunner = "ar.edu.unaj.am.myapplication.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true // BuildConfig.DEBUG: logs de red solo en debug
    }
}

dependencies {
    // :app es el "composition root": el único módulo que conoce TODAS las capas.
    // Ya no las conecta a mano: las declara, y Hilt arma el grafo juntando los
    // módulos que cada capa expone (NetworkModule, DatabaseModule, DataModule).
    implementation(project(":features:todos"))
    implementation(project(":features:books"))
    implementation(project(":features:playground"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Navigation 3: el back stack es una lista que manejamos nosotros
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    // Un ViewModelStore por pantalla del back stack (cada entrada tiene su propio ViewModel)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // hiltViewModel(): el puente entre el grafo de Hilt y los ViewModel de Compose
    implementation(libs.androidx.hilt.navigation.compose)

    testImplementation(libs.junit)

    // Tests instrumentados: corren en un emulador/dispositivo real.
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}