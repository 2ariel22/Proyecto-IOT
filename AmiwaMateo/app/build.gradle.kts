plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.rvoz"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rvoz"
        minSdk = 23
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Bibliotecas principales
    implementation("androidx.core:core-ktx:1.12.0") // Core KTX
    implementation("androidx.appcompat:appcompat:1.6.1") // AppCompatActivity
    implementation("com.google.android.material:material:1.9.0") // Material Design

    // Layouts y actividades
    implementation("androidx.activity:activity-ktx:1.8.0") // Activity KTX
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // ConstraintLayout
    implementation("com.itextpdf:itext7-core:7.1.16")

    // Redes (HTTP)
    implementation("com.squareup.okhttp3:okhttp:4.9.3") // OkHttp para peticiones HTTP

    // Dependencias de prueba
    testImplementation("junit:junit:4.13.2") // Pruebas unitarias
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // Pruebas Android
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // Espresso
}
