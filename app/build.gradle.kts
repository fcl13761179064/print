plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.android.print"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.android.print"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding=true
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
        compose = true
    }
}

dependencies {
    api(project(":print"))
    compileOnly(fileTree("dir" to "libs", "include" to listOf("*.aar")))
    
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.utilcodex)
    implementation(libs.eventbus)
    implementation("com.alibaba:fastjson:1.2.66")
}