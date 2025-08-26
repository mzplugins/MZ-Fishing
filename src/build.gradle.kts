import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "br.com.mz"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
    flatDir {
        dirs("libs")
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    implementation("com.google.inject:guice:7.0.0")
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
}

tasks.withType<ShadowJar> {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")

    relocate("com.google.inject", "br.com.mz.libs.guice")
    relocate("com.google.common", "br.com.mz.libs.guava")
    relocate("org.aopalliance", "br.com.mz.libs.aopalliance")
    relocate("jakarta.inject", "br.com.mz.libs.jakartainject")
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