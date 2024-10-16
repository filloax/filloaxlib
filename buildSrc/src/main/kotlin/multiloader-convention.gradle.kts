import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    idea

    kotlin("jvm")
}

val javaVersion: Int = (property("javaVersion")!! as String).toInt()
val javaVersionEnum = JavaVersion.values().find { it.majorVersion == javaVersion.toString() } ?: throw Exception("Cannot find java version for $javaVersion")

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)

    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = javaVersionEnum
    targetCompatibility = javaVersionEnum
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

    maven {
        name = "BlameJared"
        url = uri("https://maven.blamejared.com")
    }

    exclusiveContent {
        forRepositories(maven { url = uri("https://jitpack.io") })
        filter { includeGroup("com.github.stuhlmeier") }
    }
}

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

val version = libs.findVersion("modversion").get()
val minecraftVersion = libs.findVersion("minecraft").get()
val minecraftVersionRange = libs.findVersion("minecraft.range").get()
val fapiVersion = libs.findVersion("fabric.api").get()
val fabricVersion = libs.findVersion("fabric").get()
val fabricKotlinVersion = libs.findVersion("fabric.language.kotlin").get()
val neoforgeVersion = libs.findVersion("neoforge").get()
val neoforgeVersionRange = libs.findVersion("neoforge.range").get()
val fmlVersionRange = libs.findVersion("fml.range").get()
val kotlinforgeVersion = libs.findVersion("kotlinforge").get()
val kotlinforgeVersionRange = libs.findVersion("kotlinforge.range").get()

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${modName}" }
    }

    manifest {
        attributes(mapOf(
                "Specification-Title"     to modName,
                "Specification-Vendor"    to author,
                "Specification-Version"   to version,
                "Implementation-Title"    to modName,
                "Implementation-Version"  to version,
                "Implementation-Vendor"   to author,
                "Built-On-Minecraft"      to minecraftVersion
        ))
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.valueOf("JVM_$javaVersion"))
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
            "version_prefix" to "$version-$minecraftVersion",
            "group" to project.group, // Else we target the task's group.
            "display_url" to displayUrl, // Else we target the task's group.
            "minecraft_version" to minecraftVersion,
            "minecraft_version_range" to minecraftVersionRange,
            "fabric_api_version" to fapiVersion,
            "fabric_loader_version" to fabricVersion,
            "fabric_kotlin_version" to fabricKotlinVersion,
            "neoforge_version" to neoforgeVersion,
            "neoforge_version_range" to neoforgeVersionRange,
            "fml_version_range" to fmlVersionRange,
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

publishing {
    repositories {
        mavenLocal()
    }
}
