// Simple test mod to check less immediate features (Neo)

plugins {
    alias(libs.plugins.moddevgradle)

    kotlin("jvm")
}

val modid: String by project
val testModid: String by project
val modversion = libs.versions.modversion
val testModVersion = "0.1"
val minecraftVersion = libs.versions.minecraft.asProvider()

version = "$testModVersion-${minecraftVersion}-neoforge"

neoForge {
    version.set(libs.versions.neoforge.asProvider())

    validateAccessTransformers = true

    parchment {
        minecraftVersion = libs.versions.parchment.minecraft
        mappingsVersion = libs.versions.parchment.asProvider()
    }

    runs {
        create("client") {
            client()
        }

        create("server") {
            server()
        }

        create("gameTestServer") {
            type = "gameTestServer"
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("neoforge.enabledGameTestNamespaces", testModid)

            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        register(testModid) {
            sourceSet(sourceSets.main.get())
        }
    }
}

repositories {
    mavenCentral()

    exclusiveContent {
        forRepository {
            maven {
                name = "Sponge"
                url = uri("https://repo.spongepowered.org/repository/maven-public")
            }
        }
        filter { includeGroupAndSubgroups("org.spongepowered") }
    }

    exclusiveContent {
        forRepositories(
            maven {
                name = "ParchmentMC"
                url = uri("https://maven.parchmentmc.org/")
            },
            maven {
                name = "NeoForge"
                url = uri("https://maven.neoforged.net/releases")
            }
        )
        filter { includeGroup("org.parchmentmc.data") }
    }

    exclusiveContent {
        forRepositories(maven { url = uri("https://jitpack.io") })
        filter { includeGroup("com.github.stuhlmeier") }
    }

    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

dependencies {
    implementation( libs.kotlinforge )

    implementation( project(":neoforge") )
    {
        capabilities {
            requireCapability("$group:$modid-neoforge:$modversion-${minecraftVersion}")
        }
    }
}

val testModName: String by project
val testModDescription: String by project
val testModIcon: String by project
val author: String by project
val license: String by project
val testDisplayUrl: String by project

val minecraftVersionRange = libs.versions.minecraft.range.asProvider()
val neoforgeVersion = libs.versions.neoforge.asProvider()
val neoforgeVersionRange = libs.versions.neoforge.range
val kotlinforgeVersion = libs.versions.kotlinforge.asProvider()
val kotlinforgeVersionRange = libs.versions.kotlinforge.range

tasks.withType<ProcessResources>().configureEach {
    exclude(".cache")

    val expandProps = mapOf(
        "version_prefix" to "$testModVersion-$minecraftVersion",
        "group" to project.group,
        "display_url" to testDisplayUrl,
        "minecraft_version" to minecraftVersion,
        "minecraft_version_range" to minecraftVersionRange,
        "neoforge_version" to neoforgeVersion,
        "neoforge_version_range" to neoforgeVersionRange,
        "kotlinforge_version" to kotlinforgeVersion,
        "kotlinforge_version_range" to kotlinforgeVersionRange,
        "mod_name" to testModName,
        "author" to author,
        "mod_id" to modid,
        "license" to license,
        "description" to testModDescription,
        "mod_icon" to testModIcon,
    )

    filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
        expand(expandProps)
    }

    inputs.properties(expandProps)
}
