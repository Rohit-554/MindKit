import java.util.Properties

// AI provider keys — read from local.properties at build time.
// See local.properties.example for the expected key names.
val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) localPropsFile.inputStream().use { localProps.load(it) }
val modelDownloadUrl = localProps.getProperty(
    "mindkit.model.download.url",
    "https://YOUR_CDN_URL/models/gemma-3-270m/gemma-3-270m-onnx.zip",
)
val modelChecksumSha256 = localProps.getProperty("mindkit.model.checksum.sha256", "")
val modelExpectedSizeBytes = localProps.getProperty("mindkit.model.expected.size.bytes", "")

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.example.mindkit.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.mindkit"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["mindkitModelDownloadUrl"] = modelDownloadUrl
        manifestPlaceholders["mindkitModelChecksumSha256"] = modelChecksumSha256
        manifestPlaceholders["mindkitModelExpectedSizeBytes"] = modelExpectedSizeBytes
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("mindkit-release.jks")
            storePassword = localProps.getProperty("mindkit.keystore.storePassword")
            keyAlias = localProps.getProperty("mindkit.keystore.keyAlias")
            keyPassword = localProps.getProperty("mindkit.keystore.keyPassword")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    debugImplementation(libs.compose.uiTooling)
}
