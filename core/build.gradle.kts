plugins {
    `java-library`
}

dependencies {
    api(project(":api"))
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.cronutils:cron-utils:9.2.1")
}
