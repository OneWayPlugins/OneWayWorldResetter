plugins {
    java
}

allprojects {
    group = "net.onewaycraft"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        withSourcesJar()
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.incendo.org/snapshots/") { name = "incendo" }
        maven("https://repo.triumphteam.dev/snapshots/") { name = "triumph" }
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.10.2")
        "testImplementation"("org.mockito:mockito-core:5.11.0")
        "testImplementation"("org.assertj:assertj-core:3.25.3")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
    }
}
