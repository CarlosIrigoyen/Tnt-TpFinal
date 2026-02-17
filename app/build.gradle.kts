plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.kapt")
    id ("kotlin-parcelize")
    id("com.google.gms.google-services")



}




android {
    namespace = "com.example.trabajofinal2024"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.trabajofinal2024"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] =
                    "$projectDir/schemas"
            }
        }

    }
    buildFeatures {
        dataBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
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
    /*implementation(libs.androidx.room.common)
    val nav_version = "2.7.7"

    val roomVersion = "2.6.1"
    // Java language implementation
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Feature module Support
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    // Jetpack Compose Integration
    //implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //implementation ("androidx.appcompat:appcompat:$rootProject.appCompatVersion")
    //implementation ("androidx.activity:activity-ktx:$rootProject.activityVersion")

    // Room components
    implementation ("androidx.room:room-ktx:$roomVersion")
    kapt ("androidx.room:room-compiler:$roomVersion")
    //androidTestImplementation ("androidx.room:room-testing:$rootProject.roomVersion")

    // Lifecycle components
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:$rootProject.lifecycleVersion")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:$rootProject.lifecycleVersion")
    implementation ("androidx.lifecycle:lifecycle-common-java8:$rootProject.lifecycleVersion")

    // Kotlin components
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$nav_version")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-core:$rootProject.coroutines")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-android:$rootProject.coroutines")

    // UI
    implementation ("androidx.constraintlayout:constraintlayout:$rootProject.constraintLayoutVersion")
    implementation ("com.google.android.material:material:$rootProject.materialVersion")
*/
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // To use Kotlin annotation processing tool (kapt)
    kapt("androidx.room:room-compiler:$roomVersion")

    val lifecycleVersion = "2.7.0"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    // Saved state module for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")

    val navVersion = "2.7.7"

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")



    // Feature module Support
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navVersion")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Jetpack Compose

    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.foundation:foundation")
    // Interoperabilidad LiveData <-> Compose
    implementation("androidx.compose.runtime:runtime-livedata")



    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    //Mapa
    implementation("org.osmdroid:osmdroid-android:6.1.18")


    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    //MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //ChipGroup
    implementation("com.google.android.material:material:1.11.0")


}
