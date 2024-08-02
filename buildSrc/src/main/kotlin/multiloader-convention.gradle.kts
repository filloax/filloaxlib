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

val libs = project.versionCatalogs.find("libs")

val modid: String by project
val modName: String by project
val modDescription: String by project
val mavenGroup: String by project
val baseName: String by project
val author: String by project
val license: String by project
val displayUrl: String by project

val version = libs.get().findVersion("modversion").get()
val minecraftVersion = libs.get().findVersion("minecraft").get()
val forgeVersion = libs.get().findVersion("forge").get()
val forgeVersionRange = libs.get().findVersion("forge.range").get()
val fmlVersionRange = libs.get().findVersion("forge.fml.range").get()
val minecraftVersionRange = libs.get().findVersion("minecraft.range").get()
val fapiVersion = libs.get().findVersion("fabric.api").get()
val fabricVersion = libs.get().findVersion("fabric").get()
val fabricKotlinVersion = libs.get().findVersion("fabric.language.kotlin").get()

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
            "forge_version" to forgeVersion,
            "fml_version_range" to fmlVersionRange,
            "forge_version_range" to forgeVersionRange,
            "minecraft_version_range" to minecraftVersionRange,
            "fabric_api_version" to fapiVersion,
            "fabric_loader_version" to fabricVersion,
            "fabric_kotlin_version" to fabricKotlinVersion,
            "mod_name" to modName,
            "author" to author,
            "mod_id" to modid,
            "license" to license,
            "description" to modDescription
    )

    filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/mods.toml", "*.mixins.json")) {
        expand(expandProps)
    }

    inputs.properties(expandProps)
}

publishing {
    repositories {
        mavenLocal()
    }
}
