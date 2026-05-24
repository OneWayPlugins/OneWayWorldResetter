plugins {
    `java-library`
}

dependencies {
    api(project(":core"))
    compileOnly("dev.folia:folia-api:1.21.4-R0.1-SNAPSHOT")
}
