pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // MSAL (com.microsoft.identity:common) transitively depends on the Surface Duo
        // SDK's display-mask artifact, which Microsoft only publishes here, not on Maven
        // Central/Google.
        maven(url = "https://pkgs.dev.azure.com/MicrosoftDeviceSDK/DuoSDK-Public/_packaging/Duo-SDK-Feed/maven/v1")
    }
}

rootProject.name = "agenda"

include(
    ":app",
    ":core:common",
    ":core:model",
    ":core:domain",
    ":core:database",
    ":core:network",
    ":core:data",
    ":core:designsystem",
    ":feature:auth",
    ":feature:calendar",
    ":feature:sync",
    ":feature:mail",
    ":feature:planner",
    ":feature:activitylog",
    ":feature:reports",
)
