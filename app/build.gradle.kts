plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    // Genera el código de serialización de las rutas de navegación type-safe (@Serializable)
    alias(libs.plugins.kotlin.serialization)
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    // :app es el "composition root": el único módulo que conoce TODAS las capas
    // para poder conectarlas (inyección de dependencias manual en AppContainer).
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}