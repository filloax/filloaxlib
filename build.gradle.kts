import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.addToStdlib.flattenTo

val kotlinVersion: String by project

plugins {
    id("maven-publish")
    kotlin("jvm")
    kotlin("plugin.serialization")

//    id("com.modrinth.minotaur") version "2.+" apply false
//    id("net.darkhax.curseforgegradle") version "1.1.17" apply false
}

repositories {
    // Necessary in this file too
    // so IntelliJ can properly load kotlin stuff
    // when loading the project
    mavenCentral()
}

val javaVersion: Int = (property("javaVersion")!! as String).toInt()
val modVersion: String by project

val javaVersionEnum = JavaVersion.values().find { it.majorVersion == javaVersion.toString() } ?: throw Exception("Cannot find java version for $javaVersion")

tasks.register("modVersion") {
    println("VERSION=$modVersion")
}

subprojects {
    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")
    }

    tasks.withType<JavaCompile> {
        options.release = javaVersion
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all,-classfile,-processing,-deprecation,-serial", "-Werror"))
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
    }

    java {
        withSourcesJar()

        sourceCompatibility = javaVersionEnum
        targetCompatibility = javaVersionEnum
    }
}