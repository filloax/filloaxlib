// Multi-project setup start
// Note: as multi-version was more trouble than it was worth, I switched to a simpler one-version-per-branch setup
// Things are still leftover from the other setup

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

listOf(
    "base",
    "fabric"
).forEach { include(it) }