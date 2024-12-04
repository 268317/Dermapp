plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
}

android {

    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
        }
    }

    namespace = "com.example.dermapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dermapp"
        minSdk = 24
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.room.ktx)
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation(libs.play.services.maps)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.androidx.ui.graphics.android)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.firebase.functions.ktx)
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.play.services.maps)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.libraries.places:places:4.1.0")
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation("com.google.firebase:firebase-messaging:24.1.0")
    implementation("com.google.gms:google-services:4.4.2")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.picasso:picasso:2.71828")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp (dla niestandardowych nagłówków i logowania żądań)
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation("com.google.auth:google-auth-library-oauth2-http:1.16.0")

}
