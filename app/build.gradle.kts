plugins {
    id("com.android.application")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.aiexpensemanagementapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.aiexpensemanagementapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "BACKEND_BASE_URL",
            "\"http://10.0.2.2:3000\""
        )
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/INDEX.LIST"
        }
    }
}

dependencies {

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.17.0")

    // =========================
    // Core
    // =========================

    implementation("androidx.core:core:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.itextpdf:itext7-core:7.2.5")

    implementation("com.hbb20:ccp:2.7.3")


    // =========================
    // Firebase
    // =========================

    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))

    implementation("com.google.firebase:firebase-auth")

    implementation("com.google.firebase:firebase-firestore")

    implementation("com.google.firebase:firebase-analytics")


    // =========================
    // Google Sign-In / Gmail
    // =========================

    implementation("com.google.android.gms:play-services-auth:21.4.0")

    implementation("com.google.api-client:google-api-client:2.8.1")

    implementation("com.google.api-client:google-api-client-android:2.8.1")

    implementation("com.google.http-client:google-http-client-gson:1.47.0")

    implementation("com.google.apis:google-api-services-gmail:v1-rev20250630-2.0.0")


    // =========================
    // Retrofit
    // =========================

    implementation("com.squareup.retrofit2:retrofit:2.12.0")

    implementation("com.squareup.retrofit2:converter-gson:2.12.0")


    // =========================
    // RecyclerView
    // =========================

    implementation("androidx.recyclerview:recyclerview:1.3.2")


    // =========================
    // Lifecycle
    // =========================

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.4")

    implementation("androidx.lifecycle:lifecycle-livedata:2.8.4")

    implementation("androidx.lifecycle:lifecycle-runtime:2.8.4")


    // =========================
    // Room
    // =========================

    implementation("androidx.room:room-runtime:2.6.1")

    annotationProcessor("androidx.room:room-compiler:2.6.1")


    // =========================
    // Activity
    // =========================

    implementation("androidx.activity:activity:1.9.0")


    // =========================
    // Charts
    // =========================

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


    // =========================
    // SwipeRefreshLayout
    // =========================

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")


    // =========================
    // Testing
    // =========================

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    androidTestImplementation(
        "androidx.test.espresso:espresso-core:3.5.1"
    )
}


// =====================================================
// gRPC VERSION CONFLICT FIX
// Firebase Firestore uses gRPC.
// Gmail Google API dependencies introduce newer gRPC.
// Keep all gRPC modules on the same version.
// =====================================================

configurations.configureEach {

    resolutionStrategy.eachDependency {

        if (requested.group == "io.grpc") {

            useVersion("1.70.0")

            because(
                "Keep all gRPC modules compatible with Firebase Firestore and Google Gmail API"
            )
        }
    }
}