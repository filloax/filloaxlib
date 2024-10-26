plugins {
    java
    `maven-publish`
    idea

    kotlin("jvm")
    kotlin("plugin.serialization")

    // kotlin-compatible javadoc, cannot use base as it errors with kotlin
    id("org.jetbrains.dokka")
}

val javaVersion: Int = (property("javaVersion")!! as String).toInt()

base {
    archivesName = property("archives_base_name") as String
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)

    withSourcesJar()
//    withJavadocJar() // uses dokka for kotlin compat
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

//region Libs and props
val libs = project.versionCatalogs.find("libs").get()

val modid: String by project
val modName: String by project
val modDescription: String by project
val modIcon: String by project
val mavenGroup: String by project
val baseName: String by project
val author: String by project
val license: String by project
val displayUrl: String by project

val modversion = libs.findVersion("modversion").get()
val minecraftVersion = libs.findVersion("minecraft").get()
val minecraftVersionRange = libs.findVersion("minecraft.range").get()
val fabricMinecraftVersionRange = libs.findVersion("minecraft.range.fabric").get()
val fapiVersion = libs.findVersion("fabric.api").get()
val fabricVersion = libs.findVersion("fabric").get()
val fabricKotlinVersion = libs.findVersion("fabric.language.kotlin").get()
val neoforgeVersion = libs.findVersion("neoforge").get()
val neoforgeVersionRange = libs.findVersion("neoforge.range").get()
val kotlinforgeVersion = libs.findVersion("kotlinforge").get()
val kotlinforgeVersionRange = libs.findVersion("kotlinforge.range").get()
//endregion

//region Artifacts and publishing
// Declare capabilities on the outgoing configurations.
// Read more about capabilities here: https://docs.gradle.org/current/userguide/component_capabilities.html#sec:declaring-additional-capabilities-for-a-local-component
listOf("apiElements", "runtimeElements", "sourcesElements"/*, "javadocElements"*/).forEach { variant ->
    configurations.getByName(variant).outgoing {
        capability("$group:${base.archivesName.get()}:$modversion-${minecraftVersion}")
        capability("$group:$modid-${project.name}:$modversion-${minecraftVersion}")
        capability("$group:$modid:$modversion")
    }
    publishing.publications.withType<MavenPublication>().configureEach {
        suppressPomMetadataWarningsFor(variant)
    }
}


// Publishing
publishing {
    repositories {
        mavenLocal()
    }

    publications {
        val pubName = "${base.archivesName.get()}-${project.name}"
        register<MavenPublication>(pubName) {
            artifactId = pubName
            version = "$modversion-$minecraftVersion"
            from(components.findByName("java"))
        }
    }
}
//endregion

//region Task configuration
tasks.named<Jar>("sourcesJar") {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${modName}" }
    }
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${modName}" }
    }

    manifest {
        attributes(mapOf(
                "Specification-Title"     to modName,
                "Specification-Vendor"    to author,
                "Specification-Version"   to modversion,
                "Implementation-Title"    to modName,
                "Implementation-Version"  to modversion,
                "Implementation-Vendor"   to author,
                "Built-On-Minecraft"      to minecraftVersion
        ))
    }
}

tasks.withType<JavaCompile>().configureEach {
    this.options.encoding = "UTF-8"
    this.options.release.set(javaVersion)
    options.compilerArgs.addAll(listOf("-Xlint:all,-classfile,-processing,-deprecation,-serial", "-Xdoclint:none", "-Werror"))
}

tasks.withType<ProcessResources>().configureEach {
    exclude(".cache")

    val expandProps = mapOf(
            "version_prefix" to "$modversion-$minecraftVersion",
            "group" to project.group, // Else we target the task's group.
            "display_url" to displayUrl, // Else we target the task's group.
            "minecraft_version" to minecraftVersion,
            "minecraft_version_range" to minecraftVersionRange,
            "fabric_minecraft_version_range" to fabricMinecraftVersionRange,
            "fabric_api_version" to fapiVersion,
            "fabric_loader_version" to fabricVersion,
            "fabric_kotlin_version" to fabricKotlinVersion,
            "neoforge_version" to neoforgeVersion,
            "neoforge_version_range" to neoforgeVersionRange,
            "kotlinforge_version" to kotlinforgeVersion,
            "kotlinforge_version_range" to kotlinforgeVersionRange,
            "mod_name" to modName,
            "author" to author,
            "mod_id" to modid,
            "license" to license,
            "description" to modDescription,
            "mod_icon" to modIcon,
    )

    filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
        expand(expandProps)
    }

    inputs.properties(expandProps)
}
//endregion

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

// Use dokka for kotlin-compatible javadoc

// Make sure our token replacement runs first
val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

tasks.build {
    dependsOn(dokkaJavadocJar)
}