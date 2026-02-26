import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
}

android {
    namespace = "com.decathlon.otel.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.decathlon.otel.demo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)

    implementation(libs.jetbrains.androidx.material3.compose)
    implementation(libs.jetbrains.androidx.viewmodel.compose)
    implementation(libs.jetbrains.androidx.navigation.compose)
    implementation(libs.jetbrains.kotlinx.coroutines)
    implementation(libs.jetbrains.kotlinx.serialization.json)

    implementation(libs.bundles.io.ktor.client)

    implementation(platform(libs.io.opentelemetry.bom))
    implementation(libs.bundles.io.opentelemetry)
    implementation(libs.io.opentelemetry.instrumentation.ktor)
    implementation(libs.io.opentelemetry.sdk.trace)
    implementation(libs.io.opentelemetry.exporter.logging)
    implementation(libs.io.opentelemetry.semconv)
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    withType<JavaCompile> {
        val javaToolchains = project.extensions.getByType<JavaToolchainService>()
        javaCompiler.set(
            javaToolchains.compilerFor {
                languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_21.toString()))
            },
        )
    }
}
