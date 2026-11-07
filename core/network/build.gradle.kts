import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// OAuth client config is per-machine, like sdk.dir — read the same local.properties AGP
// already reads, instead of committing real client IDs.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "app.ilecto.agenda.core.network"
    compileSdk = 37

    defaultConfig {
        minSdk = 26

        buildConfigField(
            "String",
            "GOOGLE_OAUTH_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_OAUTH_CLIENT_ID", "")}\"",
        )
        buildConfigField(
            "String",
            "MSAL_CLIENT_ID",
            "\"${localProperties.getProperty("MSAL_CLIENT_ID", "")}\"",
        )
        buildConfigField(
            "String",
            "MSAL_REDIRECT_URI",
            "\"${localProperties.getProperty("MSAL_REDIRECT_URI", "")}\"",
        )
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))

    // api, not implementation: GoogleCalendarApi/OutlookGraphApi's delete endpoints return
    // retrofit2.Response<Unit> directly (to dodge the 204-empty-body JSON-parsing problem),
    // so core:data needs retrofit2.Response on its own compile classpath too.
    api(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    implementation(libs.msal)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
