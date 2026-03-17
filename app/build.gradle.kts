plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.albertowisdom.wisdomspark"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.albertowisdom.wisdomspark"
        minSdk = 24
        targetSdk = 36
        versionCode = 9
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API keys desde gradle.properties (NO hardcodear en código fuente)
        buildConfigField("String", "SHEETS_API_KEY", "\"${project.findProperty("sheetsApiKey") ?: ""}\"")
        buildConfigField("String", "SHEETS_SPREADSHEET_ID", "\"${project.findProperty("sheetsSpreadsheetId") ?: ""}\"")
        buildConfigField("String", "ADMOB_BANNER_ID", "\"${project.findProperty("admobBannerId") ?: "ca-app-pub-3940256099942544/6300978111"}\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"${project.findProperty("admobInterstitialId") ?: "ca-app-pub-3940256099942544/1033173712"}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("/mnt/c/Users/alber/Documents/PROYECTOS/KeyStore/android_key_store.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: project.findProperty("keystorePassword") as String?
            keyAlias = System.getenv("KEY_ALIAS") ?: project.findProperty("keyAlias") as String? ?: "key0"
            keyPassword = System.getenv("KEY_PASSWORD") ?: project.findProperty("keyPassword") as String?
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${layout.buildDirectory.asFile.get()}/compose_metrics"
        )
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${layout.buildDirectory.asFile.get()}/compose_metrics"
        )
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    // Configure JUnit5
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
            all {
                it.useJUnitPlatform()
            }
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // AdMob (opcional)
    implementation(libs.play.services.ads)
    
    // Google Play Services Base (for integrity checks)
    implementation(libs.play.services.base)
    
    // DataStore (para preferencias)
    implementation(libs.androidx.datastore.preferences)
    
    // Material Components (para compatibilidad con XML themes)
    implementation(libs.material.components)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // Haze (for glassmorphism effects)
    implementation(libs.haze)
    
    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    
    // Play Integrity API
    implementation(libs.play.integrity)
    
    // Google Play Billing
    implementation(libs.play.billing.ktx)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
    // JUnit5 Support
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    
    // Mockito for Unit Tests
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    
    // Coroutines Testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    
    // Turbine for Flow testing
    testImplementation("app.cash.turbine:turbine:1.0.0")
    
    // Robolectric for Android Unit Tests
    testImplementation("org.robolectric:robolectric:4.11.1")
    
    // Compose Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:2024.12.01")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    
    // Hilt Testing
    testImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48.1")
    kspTest("com.google.dagger:hilt-compiler:2.48.1")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.48.1")
}

