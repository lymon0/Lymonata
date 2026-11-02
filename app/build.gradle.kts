plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.lymon.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lymon.app"
        minSdk = 24
        targetSdk = 35
        versionCode = (System.getenv("BITRISE_BUILD_NUMBER") ?: "1").toInt()
        versionName = "1.0"
        val api = System.getenv("LYMON_API_URL")
            ?: (project.findProperty("LYMON_API_URL") as String?)
            ?: "https://script.google.com/macros/s/REPLACE_ME/exec"
        buildConfigField("String", "API_URL", "\"$api\"")
    }

    buildFeatures { compose = true; buildConfig = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    val bom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(bom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
