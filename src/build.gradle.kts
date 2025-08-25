plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "br.com.mz"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    implementation("com.google.inject:guice:7.0.0")
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

val serverDir: String by project

tasks.register<Copy>("copyJarToServer") {
    from(tasks.shadowJar)
    into(serverDir)
}

tasks.build {
    finalizedBy(tasks.named("copyJarToServer"))
}