plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.programe.print"
    compileSdk = 35


    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            // 关闭混淆，避免第三方依赖类被移除导致 NoClassDefFoundError
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
    
    // 打包配置
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// 解决依赖版本冲突 - 强制使用兼容 UniApp 的旧版本
configurations.configureEach {
    resolutionStrategy {
        force("androidx.appcompat:appcompat:1.2.0")
        force("androidx.appcompat:appcompat-resources:1.2.0")
        force("androidx.core:core:1.3.2")
        force("androidx.core:core-ktx:1.3.2")
        force("androidx.lifecycle:lifecycle-runtime:2.2.0")
        force("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
        force("androidx.lifecycle:lifecycle-common:2.2.0")
        force("androidx.activity:activity:1.1.0")
        force("androidx.fragment:fragment:1.2.5")
        force("androidx.annotation:annotation:1.1.0")
    }
}

dependencies {
    // UniApp SDK 保持 compileOnly，因为宿主 APP 会提供
    compileOnly(fileTree(mapOf("dir" to "../app/libs", "include" to listOf("uniapp-v8-release.aar"))))
    
    // AndroidX 基础库 - compileOnly 因为宿主会提供
    compileOnly("androidx.core:core-ktx:1.3.2")
    compileOnly("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    compileOnly("androidx.appcompat:appcompat:1.2.0")
    compileOnly(libs.firebase.crashlytics.buildtools)
    
    // EventBus 用于打印完成事件通知 - 必须 api，因为多个类直接使用
    api(libs.eventbus)
    
    // utilcodex 工具库 - 必须 api，因为代码中直接使用 PermissionUtils、ToastUtils、GsonUtils 等
    api("com.blankj:utilcodex:1.31.1") {
        // 排除会打包进 AAR 的 AndroidX 依赖（宿主会提供）
        exclude(group = "androidx.core")
        exclude(group = "androidx.lifecycle")
        exclude(group = "androidx.fragment")
        exclude(group = "androidx.activity")
        // 保留 appcompat，因为 utilcodex 资源需要它编译
    }
    
    // Gson - 必须 api，因为 utilcodex 的 GsonUtils 需要，且 BtService 中使用
    api("com.google.code.gson:gson:2.10.1")
    
    // fastjson 用于 JSON 解析 - 必须 api，因为 SearchBluetoothActivity 中直接使用
    api("com.alibaba:fastjson:1.2.83")
}