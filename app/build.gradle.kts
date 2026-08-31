plugins {
    id("com.android.application")
}

android {
    namespace = "com.fireos8.settingshub"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig {
        applicationId = "com.fireos8.settingshub"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.leanback:leanback:1.2.0-alpha04")
}
