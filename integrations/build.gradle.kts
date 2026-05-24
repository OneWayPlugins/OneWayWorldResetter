plugins {
    `java-library`
}

dependencies {
    api(project(":api"))
    compileOnly(project(":core"))
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.popcraft:chunky-common:1.4.36")
    implementation("com.google.code.gson:gson:2.10.1")
}
