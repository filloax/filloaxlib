import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom")
    id("maven-publish")
}

val minecraftVersion: String by project
val modid: String by project

loom {
    splitEnvironmentSourceSets()

    accessWidenerPath = file("src/main/resources/fxlib.accesswidener")
    mixin.defaultRefmapName = "fxlib.refmap.json"


    mods {
        register(modid) {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
    }
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
val baseName: String by project
val author: String by project

val fabricKotlinVersion: String by project
val parchmentVersion: String by project
val fabricApiVersion: String by project
val fabricLoaderVersion: String by project

version = "$modVersion-${minecraftVersion}-fabric"
group = mavenGroup

base {
    archivesName = baseName
}

val baseProject = project(":base")

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    //mappings("net.fabricmc:yarn:${property("yarnMappings")}:v2")
    mappings(loom.layered() {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchmentVersion}@zip")
    })

    compileOnly(baseProject)

    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${fabricKotlinVersion}")
    // include("net.fabricmc:fabric-language-kotlin:${property("fabricKotlinVersion")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}") {
        exclude(module = "fabric-api-deprecated")
    }
}

tasks.compileJava {
    source(baseProject.sourceSets.getByName("main").allSource)
}

tasks.compileKotlin  {
    source(baseProject.sourceSets.getByName("main").allSource)
}


tasks.getByName<Jar>("sourcesJar") {
    val mainSourceSet = baseProject.sourceSets.getByName("main")
    from(mainSourceSet.allSource)
}
tasks.kotlinSourcesJar {
    val mainSourceSet = baseProject.sourceSets.getByName("main")
    from(mainSourceSet.allSource)
}

tasks.processResources {
    exclude(".cache")
    inputs.property("version", project.version)

    from(baseProject.sourceSets.getByName("main").resources)
    exclude(
        "fxlib_base.accesswidener",
        "fxlib_base.mixins.json",
    )

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

/*
tasks.register("curseforge", TaskPublishCurseForge::class) {
    disableVersionDetection()
    apiToken = System.getenv("CURSEFORGE_TOKEN")
    val projectId = System.getenv("CURSEFORGE_PROJECT_ID")
    val mainFile = upload(projectId, if (useLoom != null) remapJar else jar)
    mainFile.addModLoader(projectExt.platform.get())
    projectExt.supportedMinecraftVersions.getOrElse(emptyList()).forEach {
        mainFile.addGameVersion(it)
    }
    mainFile.releaseType = "release"
    mainFile.changelog = "Bug fixes"
}
*/

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to modid,
                "Specification-Version" to modVersion,
                "Specification-Vendor" to author,
                "Implementation-Title" to "$modid-fabric",
                "Implementation-Version" to version,
                "Implementation-Vendor" to author,
            )
        )
    }
}