
plugins {
    java
    kotlin("jvm")
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
    kotlin("plugin.serialization") version "1.9.23"
}

ext["mcVersion"] = "1.20.4"
ext["platform"] = "Vanilla"
ext["supported"] = listOf("1.20.4")

minecraft {
    version("1.20.4")

    // Uncomment this to enable the usage of Access Widener
    accessWideners("src/main/resources/fxlib.accesswidener")
}

val kotlinVersion = property("kotlin_version") as String
val kotlinxSerializationVersion = property("kotlinx_serialization_version") as String

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.5")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
}

tasks.withType<JavaCompile> {
    options.release = 17
    options.encoding = "UTF-8"
}
