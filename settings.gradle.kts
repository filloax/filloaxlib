// Only used if not in multi-project setup

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
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}

val modid: String by settings

rootProject.name = modid

include("shared")
include("1.20.4-base", "1.20.4-fabric")
