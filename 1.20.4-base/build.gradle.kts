import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    java
    kotlin("jvm")
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
    kotlin("plugin.serialization") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

ext["mcVersion"] = "1.20.4"
ext["platform"] = "Vanilla"
ext["supported"] = listOf("1.20.4")

minecraft {
    version("1.20.4")

    // Used only in dev, in actual mod uses the platform AW
    accessWideners("src/main/resources/fxlib_base.accesswidener")
}

val kotlinVersion = property("kotlin_version") as String
val kotlinxSerializationVersion = property("kotlinx_serialization_version") as String

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.5")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    compileOnly("io.github.llamalad7:mixinextras-fabric:0.2.2")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "2000"))
}