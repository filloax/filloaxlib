import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.spongepowered.gradle.vanilla")
    id("com.github.johnrengelman.shadow")
}

val minecraftVersion: String by project

minecraft {
    version(minecraftVersion)

    // Used only in dev, in actual mod uses the platform AW
    accessWideners("src/main/resources/fxlib_base.accesswidener")
}

repositories {
    mavenCentral()
    maven("https://api.modrinth.com/maven")
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }
}

val modVersion: String by project
val mavenGroup: String by project
val modid: String by project
val baseName: String by project
val author: String by project

val mixinVersion: String by project
val mixinExtrasVersion: String by project
val kotlinVersion: String by project
val kotlinxSerializationVersion: String by project

version = "$modVersion-${minecraftVersion}-base"
group = mavenGroup

base {
    archivesName = baseName
}

dependencies {
    compileOnly("org.spongepowered:mixin:$mixinVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    compileOnly("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")
}

tasks.processResources {
    exclude(".cache")
    inputs.property("version", project.version)
}

// Figure this out when needed
//sourceSets.main.get().resources {
//    source(SourceDirectorySet("src/generated/resources"))
//}

//tasks.named("modrinth") {
//    enabled = false
//}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to modid,
                "Specification-Version" to modVersion,
                "Specification-Vendor" to author,
                "Implementation-Title" to modid,
                "Implementation-Version" to version,
                "Implementation-Vendor" to author,
            )
        )
    }
}