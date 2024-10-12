// Multi-project setup start
// Note: as multi-version was more trouble than it was worth, I switched to a simpler one-version-per-branch setup
// Things are still leftover from the other setup

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        // Loom (Fabric)
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
            content {
                includeGroupAndSubgroups("net.fabricmc")
                includeGroup("fabric-loom")
            }
        }
        maven {
            name = "Forge"
            url = uri("https://maven.minecraftforge.net/")
            content {
                includeGroupAndSubgroups("net.minecraftforge")
            }
        }
        maven {
            name = "Parchment"
            url = uri("https://maven.parchmentmc.org")
            content {
                includeGroupAndSubgroups("org.parchmentmc")
            }
        }
        maven {
            name = "Sponge"
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
            content {
                includeGroupAndSubgroups("org.spongepowered")
            }
        }
    }

    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion apply false
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val modid: String by settings

rootProject.name = modid

listOf(
    "base",
    "fabric"
).forEach { include(it) }