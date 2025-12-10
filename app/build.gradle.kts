import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-parcelize")
}

// local.properties 사용
val properties = Properties()
properties.load(rootProject.file("local.properties").inputStream())

val G_MAP_API_KEY = properties.getProperty("G_MAP_API_KEY")

android {
    namespace = "com.cc.near_restaurant_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cc.near_restaurant_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "PLACES_API_KEY",
            "\"${properties.getProperty("PLACES_API_KEY")}\""
        )

        buildConfigField(
            "String",
            "NEW_PLACES_API_KEY",
            "\"${properties.getProperty("NEW_PLACES_API_KEY")}\""
        )

        // 기존 buildConfigField 그대로 둬도 상관없지만 Manifest 치환을 위해 placeholder 추가
        manifestPlaceholders["G_MAP_API_KEY"] = G_MAP_API_KEY
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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //google map
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("com.google.android.libraries.places:places:3.3.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
}