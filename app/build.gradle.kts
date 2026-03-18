import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.google.hilt)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "com.paligot.otel.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.paligot.otel.demo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
            buildConfigField("String", "BASE_URL_OTEL_COLLECTOR", "\"http://10.0.2.2:4318\"")
            buildConfigField("String", "EVENT_ID", "\"81ed9849-7a70-4429-91e0-6409440fd80d\"")
            buildConfigField("String", "EVENT_URL", "\"devlille.fr\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
            buildConfigField("String", "BASE_URL_OTEL_COLLECTOR", "\"http://10.0.2.2:4318\"")
            buildConfigField("String", "EVENT_ID", "\"81ed9849-7a70-4429-91e0-6409440fd80d\"")
            buildConfigField("String", "EVENT_URL", "\"devlille.fr\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)

    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.jetbrains.androidx.material.icons)
    implementation(libs.jetbrains.androidx.material3.compose)
    implementation(libs.jetbrains.androidx.viewmodel.compose)
    implementation(libs.jetbrains.androidx.navigation.compose)
    implementation(libs.jetbrains.kotlinx.coroutines)
    implementation(libs.jetbrains.kotlinx.serialization.json)

    implementation(libs.io.coil.compose)
    implementation(libs.io.coil.network.ktor)
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
