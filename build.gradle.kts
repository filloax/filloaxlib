import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.addToStdlib.flattenTo

val kotlinVersion: String by project

plugins {
    id("maven-publish")
    kotlin("jvm")
    kotlin("plugin.serialization")

    id("org.spongepowered.gradle.vanilla") apply false
    id("com.github.johnrengelman.shadow") apply false

    id("fabric-loom") apply false

//    id("com.modrinth.minotaur") version "2.+" apply false
//    id("net.darkhax.curseforgegradle") version "1.1.17" apply false
}

//val projectDependencies: Map<String, String> by extra
//(settings["projectDependencies"] as Map<String, String>).forEach(::println)
val projectDependencies: Map<String, Set<String>> by gradle.extra

fun getProjectsToInclude(forProject: String): Set<String> {
    var current = setOf(forProject)
    val out = mutableSetOf<String>()
    while (current.isNotEmpty()) {
        current = current.mapNotNull { projectDependencies[it] }.flattenTo(mutableSetOf())
        out.addAll(current.map{":$it"})
    }
    return out
}

repositories {
    // Necessary in this file too
    // so IntelliJ can properly load kotlin stuff
    // when loading the project
    mavenCentral()
}

val javaVersion: Int = (property("javaVersion")!! as String).toInt()
val versionProp = property("mod_version") as String
val groupProp = property("maven_group") as String
val modid: String by project
val archivesBaseNameProp = property("archives_base_name") as String
val authorProp = property("author") as String
val debugDependencies = property("debugDependencies") == "true"
val commonProjectName: String by project

val javaVersionEnum = JavaVersion.values().find { it.majorVersion == javaVersion.toString() } ?: throw Exception("Cannot find java version for $javaVersion")

tasks.register("modVersion") {
    println("VERSION=$versionProp")
}

/* Unused as idk how to make it work with kotlin DSL
interface PlatformInfoExtension {
    // Which platform (i.e. Fabric/Forge/Quilt) is this implementation is on?
    // In case of projects uses solely VanillaGradle, here we use 'Vanilla'.
    val platform: Property<String>
    // Which Minecraft version is this implementation based on?
    val minecraftVersion: Property<String>
    // Which Minecraft version is this implementation compatible with?
    val supportedMinecraftVersions: ListProperty<String>
}*/
data class PlatformInfo(
    val minecraftVersion: Property<String>,
    val platform: Property<String>,
    val supportedMinecraftVersions: ListProperty<String>
) {
    constructor(ext: ExtraPropertiesExtension) : this(
        objects.property(String::class.java).value(ext["mcVersion"] as String),
        objects.property(String::class.java).value(ext["platform"] as String),
        objects.listProperty(String::class.java).value(ext["supported"] as List<String>),
    )
}

val mainProject = project
val projsToInclude = mutableMapOf<String, Set<Project>>()

subprojects {
    projsToInclude[name] = getProjectsToInclude(name).map { project(it) }.toSet()
}

println("Setup projects to include tree: ${projsToInclude.entries.joinToString("\n")}")

subprojects {
    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")

        if ("base" in name) {
            plugin("org.spongepowered.gradle.vanilla")
            plugin("com.github.johnrengelman.shadow")
        } else {
            plugin("maven-publish")
        }

        if ("fabric" in name) {
            plugin("fabric-loom")
        }

//        plugin("com.modrinth.minotaur")
//        plugin("net.darkhax.curseforgegradle")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
        withSourcesJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
    }

    version = versionProp
    group = groupProp

    repositories {
        mavenCentral()
        maven("https://api.modrinth.com/maven")
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
        }
    }

    tasks.withType<ProcessResources> {
        exclude(".cache")
        inputs.property("version", project.version)
    }

    if (name.endsWith(commonProjectName)) return@subprojects

    val projectsToInclude = projsToInclude[name]!!

    dependencies {
        projectsToInclude.forEach { compileOnly(it) }
    }

//    println("Setup dependencies for $name: are ${project.configurations.map { "\n\t${it.name}: ${it.dependencies.map {d -> d.name}}" } }")

    afterEvaluate {
        val projectExt = PlatformInfo(ext)

        version = "$version-${projectExt.minecraftVersion.get()}-${projectExt.platform.get().lowercase()}"

        val isVanilla = projectExt.platform.get() == "Vanilla"
        val isFabric = projectExt.platform.get() == "Fabric"

        tasks.compileJava {
            options.compilerArgs.addAll(listOf("-Xlint:all,-classfile,-processing,-deprecation,-serial", "-Werror"))

            projectsToInclude.forEach {
                source(it.sourceSets.getByName("main").allSource)
            }
        }

        tasks.compileKotlin  {
            projectsToInclude.forEach {
                source(it.sourceSets.getByName("main").allSource)
            }
        }

        val compileKotlin: KotlinCompile by tasks
        compileKotlin.kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
        val compileTestKotlin: KotlinCompile by tasks
        compileTestKotlin.kotlinOptions {
            jvmTarget = javaVersion.toString()
        }

        tasks.getByName<Jar>("sourcesJar") {
            projectsToInclude.forEach {
                val mainSourceSet = it.sourceSets.getByName("main")
                from(mainSourceSet.allSource)
            }
        }
        tasks.kotlinSourcesJar {
            projectsToInclude.forEach {
                val mainSourceSet = it.sourceSets.getByName("main")
                from(mainSourceSet.allSource)
            }
        }

        tasks.processResources {
            exclude(".cache")
            projectsToInclude.forEach {
                from(it.sourceSets.getByName("main").resources)
            }
            inputs.property("version", project.version)

            if (isFabric) {
                filesMatching("fabric.mod.json") {
                    expand(mapOf("version" to project.version))
                }
            }
        }

        if (debugDependencies) {
//        println("Setup dependencies for $name: are ${project.configurations.map { "\n\t${it.name}: ${it.dependencies.map {d -> d.name}}" } }")
            println("Setup sources for $name J are:\n\t${tasks.compileJava.get().source.files.joinToString("\n\t")}")
            println("Setup sources for $name K are:\n\t${tasks.compileKotlin.get().sources.joinToString("\n\t")}")
        }

        base {
//            archivesName = ("$archivesBaseNameProp-${projectExt.platform.get().lowercase()}-${projectExt.minecraftVersion.get()}")
            archivesName = archivesBaseNameProp
        }

        if (isVanilla) {
//            tasks.named("modrinth") {
//                enabled = false
//            }
        } else {
//            tasks.register("curseforge", TaskPublishCurseForge::class) {
//                disableVersionDetection()
//                apiToken = System.getenv("CURSEFORGE_TOKEN")
//                val projectId = System.getenv("CURSEFORGE_PROJECT_ID")
//                val mainFile = upload(projectId, if (useLoom != null) remapJar else jar)
//                mainFile.addModLoader(projectExt.platform.get())
//                projectExt.supportedMinecraftVersions.getOrElse(emptyList()).forEach {
//                    mainFile.addGameVersion(it)
//                }
//                mainFile.releaseType = "release"
//                mainFile.changelog = "Bug fixes"
//            }
            // configure the maven publication
            publishing {
                publications {
                    create<MavenPublication>("mavenJava") {
                        from(components["java"])
                        groupId = project.group.toString()
                        artifactId = project.archivesName.get()
                        version = project.version.toString()
                        println("MAVEN: $groupId $artifactId $version")
                    }
                }

                // select the repositories you want to publish to
                repositories {
                    // uncomment to publish to the local maven
                    mavenLocal()
                }
            }
        }

        tasks.named<Jar>("jar") {
            manifest {
                attributes(
                    mapOf(
                        "Specification-Title" to modid,
                        "Specification-Vendor" to authorProp,
                        "Specification-Version" to versionProp,
                        "Implementation-Title" to modid,
                        "Implementation-Version" to version,
                        "Implementation-Vendor" to authorProp,
                    )
                )
            }
        }
    }
}