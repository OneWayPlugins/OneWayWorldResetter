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
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.triumphteam.dev/snapshots/") { name = "triumph-snapshots" }
        maven("https://repo.triumphteam.dev/releases/") { name = "triumph-releases" }
        maven("https://repo.extendedclip.com/releases/") { name = "extendedclip" }
        maven("https://repo.codemc.io/repository/maven-public/") { name = "codemc" }
        maven("https://jitpack.io") { name = "jitpack" }
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
