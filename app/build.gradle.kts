plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlinx-serialization")
    id("kotlin-parcelize")
    id("com.github.triplet.play") version "3.9.1"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.selbiconsulting.elog"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.selbiconsulting.elog"
        minSdk = 27
        targetSdk = 34
        versionCode = 13
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {

        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("app/elog.jks")
            storePassword = "elogAiTablet"
            keyAlias = "key0"
            keyPassword = "elogAiTablet"
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
        dataBinding = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(files("libs/EldBleLib-release.aar"))

    implementation("no.nordicsemi.android.support.v18:scanner:1.6.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")
    implementation("com.google.firebase:firebase-messaging:24.0.0")

    // Architecture Components
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

    //Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    testImplementation("org.mockito:mockito-core:2.25.1")
    androidTestImplementation("org.mockito:mockito-android:4.4.0")


    //Play Services
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    //Lifecycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    //Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    //Signature Pad
    implementation("com.github.gcacace:signature-pad:1.3.1")

    //PDF VIEW
    implementation("com.dmitryborodin:pdfview-android:1.1.0")

    //Graph View
    implementation("com.jjoe64:graphview:4.2.2")

    //RoundedImageView
    implementation("com.makeramen:roundedimageview:2.3.0")

    //HILT
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-android-compiler:2.48.1")

    //BLE
    implementation("no.nordicsemi.android:ble-ktx:2.7.3")

    //Ktor
    implementation("io.ktor:ktor-client-android:1.6.7")
    implementation("io.ktor:ktor-client-json:1.6.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("io.ktor:ktor-client-logging:1.6.7")
    implementation("io.ktor:ktor-client-serialization:1.6.7")

    //logging
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    //Room
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    implementation("com.google.code.gson:gson:2.10.1")

    //Rating Bar
    implementation("com.github.ome450901:SimpleRatingBar:1.5.1")

    //Coil
    implementation("io.coil-kt:coil:2.5.0")
    implementation("io.coil-kt:coil-svg:1.4.0")

    //ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-common:1.3.1")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-inappmessaging-display")



}