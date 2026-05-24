rootProject.name = "OneWayWorldResetter"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_SETTINGS
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

include(
    "api",
    "core",
    "platform-paper",
    "platform-folia",
    "integrations",
    "plugin"
)
