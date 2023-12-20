import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("fabric-loom") version "1.4.4"
    id("maven-publish")
//    `java-library`
}

version = property("mod_version")!! as String
group = property("maven_group")!! as String
val modid: String by project

base {
    archivesName.set(property("archivesBaseName") as String)
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven("https://api.modrinth.com/maven")
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }
    maven("https://jitpack.io")
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("fxlib") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
    }

//    accessWidenerPath = file("src/main/resources/fxlib.accesswidener")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    //mappings("net.fabricmc:yarn:${property("yarnMappings")}:v2")
    mappings(loom.layered() {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_version")}@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    // include("net.fabricmc:fabric-language-kotlin:${property("fabricKotlinVersion")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}") {
        exclude(module = "fabric-api-deprecated")
    }
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

loom.runs.matching{ it.name != "data" }.configureEach {
    this.vmArg("-Dmixin.debug.export=true")
}

tasks.withType<JavaCompile> {
    options.release = 17
    options.encoding = "UTF-8"
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // select the repositories you want to publish to
    repositories {
        // uncomment to publish to the local maven
        // mavenLocal()
    }
}