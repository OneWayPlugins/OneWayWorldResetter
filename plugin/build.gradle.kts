plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.10"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))
    implementation(project(":platform-paper"))
    implementation(project(":platform-folia"))
    implementation(project(":integrations"))
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("org.incendo:cloud-annotations:2.0.0")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.10")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveBaseName.set("OneWayWorldResetter")
        relocate("org.incendo.cloud", "net.onewaycraft.owwr.libs.cloud")
        relocate("dev.triumphteam.gui", "net.onewaycraft.owwr.libs.gui")
        mergeServiceFiles()
        minimize {
            // platform-* contain classes loaded reflectively at runtime (schedulers,
            // event listeners, command handlers); minimize would strip them.
            exclude(project(":platform-paper"))
            exclude(project(":platform-folia"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
    processResources {
        val props = mapOf("version" to project.version.toString())
        filesMatching("paper-plugin.yml") { expand(props) }
    }
    runServer {
        minecraftVersion("1.21.1")
        jvmArgs("-Xms2G", "-Xmx2G")
    }
}
