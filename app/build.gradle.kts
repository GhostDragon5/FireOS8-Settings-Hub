plugins {
    id("com.android.application")
}

val releaseVersionCode = providers.gradleProperty("versionCode").orNull?.toIntOrNull() ?: 2
val releaseVersionName = providers.gradleProperty("versionName").orNull ?: "1.1.0"
val releaseStoreFile = providers.gradleProperty("releaseStoreFile").orNull
val releaseStorePassword = providers.gradleProperty("releaseStorePassword").orNull
val releaseKeyPassword = providers.gradleProperty("releaseKeyPassword").orNull

android {
    namespace = "com.fireos8.settingshub"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.fireos8.settingshub"
        minSdk = 23
        targetSdk = 28
        versionCode = releaseVersionCode
        versionName = releaseVersionName
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = "fireos8-settings-hub"
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (releaseStoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

dependencies {
    implementation("androidx.leanback:leanback:1.2.0-alpha04")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
