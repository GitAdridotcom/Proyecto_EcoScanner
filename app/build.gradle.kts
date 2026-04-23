plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Añadimos el plugin de serialización para que buildJsonObject funcione correctamente
    kotlin("plugin.serialization") version "1.9.23"
}

android {
    namespace = "com.example.ecoscanner"
    compileSdk = 36 // Cambiado a 35 (Android 15) por estabilidad, la 36 es preview técnica.

    defaultConfig {
        applicationId = "com.example.ecoscanner"
        minSdk = 24
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Dependencias base de Android/Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("io.ktor:ktor-client-logging:2.3.12")   // AÑADIR ESTA
    implementation("io.ktor:ktor-client-auth:2.3.12")      // AÑADIR ESTA
    implementation("io.ktor:ktor-client-plugins:2.3.12")
    implementation("io.github.jan-tennert.supabase:supabase-kt:VERSION")
    // ==========================================================
    // SUPABASE - CONFIGURACIÓN V3.0.0 (CORREGIDA)
    // ==========================================================

    // 1. Usamos el BOM para sincronizar todas las librerías de Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))

    // 2. Módulos obligatorios (IMPORTANTE: gotrue-kt ha sido eliminado en esta versión)
    implementation("io.github.jan-tennert.supabase:auth-kt")      // Sustituye a gotrue
    implementation("io.github.jan-tennert.supabase:postgrest-kt") // Base de Datos

    // 3. Motores de Red y Serialización (Necesarios para que Supabase hable con el servidor)
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Tests y Debug
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}