// Multi-project setup start

pluginManagement {
    repositories {
        mavenCentral()
        // Loom (Fabric)
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        // MixinGradle, VanillaGradle
        maven {
            name = "Sponge Snapshots"
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
        }
        gradlePluginPortal()
    }

    val kotlinVersion: String by settings
    val vanillaGradleVersion: String by settings
    val shadowVersion: String by settings
    val loomVersion: String by settings

    plugins {
        java apply false
        kotlin("jvm") version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion apply false
        id("org.spongepowered.gradle.vanilla") version vanillaGradleVersion apply false
        id("com.github.johnrengelman.shadow") version shadowVersion apply false
        id("fabric-loom") version loomVersion apply false
    }
}

val modid: String by settings

rootProject.name = modid

val commonProjectName: String by settings

// Dependencies in the sub-projects.
// key-value means the key projects depends on the value project
// and should include its sources.
// For example, 1.20.4-fabric depends on 1.20.4-base, which depends on 1.20.x-base, etc
val projectDependencies by gradle.extra(mapOf(
    "1.20.6-base" to setOf(commonProjectName),
    "1.20.4-fabric" to setOf("1.20.4-base"),
    "1.20.4-base" to setOf(commonProjectName),
))

val projectJavaVersions by gradle.extra(mapOf(
    17 to setOf(commonProjectName, "1.20.x-base", "1.20.4-base", "1.20.4-fabric"),
    21 to setOf("1.20.6-base"),
))

val allProjects = (projectDependencies.keys + projectDependencies.values.flatten()).toSet()

allProjects.forEach(::include)