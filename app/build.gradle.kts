plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.amazons3uploadandfetchimage"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.amazons3uploadandfetchimage"
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
        dataBinding = true
        viewBinding = true
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

    implementation ("com.github.bumptech.glide:glide:4.13.0")
    annotationProcessor  ("com.github.bumptech.glide:compiler:4.12.0")

    // dagger hilt implementation
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation ("com.amazonaws:aws-android-sdk-s3:2.22.6")
    implementation ("com.amazonaws:aws-android-sdk-core:2.22.6")



    // AWS SDK S3 Client
//    implementation ("software.amazon.awssdk:s3:2.20.52")
//
//    // Add any additional AWS SDK modules you might need
//    implementation ("software.amazon.awssdk:core:2.20.52")
//    implementation ("software.amazon.awssdk:auth:2.20.52")
//
//    // Optional: If you use regions or want better logging support
//    implementation ("software.amazon.awssdk:regions:2.20.52")
//    implementation ("software.amazon.awssdk:logging:2.20.52")
}
kapt {
    correctErrorTypes = true

}