plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}


android {
    namespace = "com.example.bimapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bimapp"
        minSdk = 24
        targetSdk = 33
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
    buildFeatures{
        viewBinding = true
        dataBinding = true
    }


}





dependencies {
    

    // MediaPipe Library
    implementation("com.google.mediapipe:tasks-vision:latest.release")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // CameraX core library
    val cameraxVersion = "1.2.0-alpha02"
    implementation("androidx.camera:camera-core:$cameraxVersion")

    // CameraX Camera2 extensions
    implementation("androidx.camera:camera-camera2:$cameraxVersion")

    // CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")

    // CameraX View class
    implementation("androidx.camera:camera-view:1.3.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")

    implementation("androidx.appcompat:appcompat:1.3.0")


    // Pytorch
    implementation ("org.pytorch:pytorch_android_lite:1.13.1")
    implementation ("org.pytorch:pytorch_android_torchvision_lite:1.13.1")


    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}