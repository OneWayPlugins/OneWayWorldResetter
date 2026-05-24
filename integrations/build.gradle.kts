plugins {
    `java-library`
}

dependencies {
    api(project(":api"))
    compileOnly(project(":core"))
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.10.1")
}
