plugins {
    `java-library`
}

dependencies {
    api(project(":api"))
    compileOnly(project(":core"))
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    // Soft-dep integrations (Multiverse, PAPI, Vault) will be wired in later phases
    // when their adapters are implemented (Phase 3 for Multiverse, Phase 9 for PAPI/Vault).
}
