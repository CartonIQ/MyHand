plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.myhand.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.myhand.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"
    }
    signingConfigs {
        create("release") {
            storeFile = file(System.getProperty("user.home") + "/myhand.jks")
            storePassword = System.getenv("MYHAND_STORE_PASS") ?: ""
            keyAlias = "myhand"
            keyPassword = System.getenv("MYHAND_KEY_PASS") ?: ""
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("com.google.android.gms:play-services-ads:23.3.0")
    implementation("com.google.android.ump:user-messaging-platform:2.2.0")
    implementation("androidx.core:core:1.13.1")
}
