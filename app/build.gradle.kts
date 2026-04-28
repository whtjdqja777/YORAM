plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.yoram"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.yoram"
        minSdk = 33
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
        packagingOptions {
            jniLibs {
                useLegacyPackaging = true
            }
        }

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
    buildFeatures {
        mlModelBinding = true
    }
}
dependencies {
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.camera:camera-core:1.1.0")
    implementation("androidx.camera:camera-camera2:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.3.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.3.0")
    implementation("com.google.android.gms:play-services-tflite-acceleration-service:16.0.0-beta01")
    implementation("com.exyte:animated-navigation-bar:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//    implementation("com.google.mediapipe:tasks-vision:latest.release")
    implementation("com.google.mediapipe:tasks-vision:0.10.18")
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
}