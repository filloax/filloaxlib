import org.gradle.api.internal.provider.AbstractProperty
import org.gradle.api.internal.provider.EvaluationContext
import org.gradle.api.internal.provider.PropertyHost
import org.gradle.api.internal.provider.ValueSupplier
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    kotlin("jvm")
    id("maven-publish")

//    id("com.modrinth.minotaur") version "2.+" apply false
//    id("net.darkhax.curseforgegradle") version "1.1.17" apply false
}

repositories {
    // Necessary in this file too
    // so IntelliJ can properly load kotlin stuff
    // when loading the project
    mavenCentral()
}

val versionProp = property("mod_version") as String
val groupProp = property("maven_group") as String
val modid: String by project
val archivesBaseNameProp = property("archives_base_name") as String
val authorProp = property("author") as String
val debugDependencies = property("debugDependencies") == "true"

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
val baseProjectSuffix = "base"
val commonProjectName = "shared"

val projsToInclude = mutableMapOf<String, MutableSet<Project>>()

subprojects {
    projsToInclude[name] = mutableSetOf<Project>().also {
        if (name.endsWith(commonProjectName)) return@also
        it.add(project(":$commonProjectName"))
        if (!name.endsWith(baseProjectSuffix)) {
            val baseProject = ":${name.substringBefore('-')}-base"
            it.add(project(baseProject))
        }
    }
}

subprojects {
    apply {
        plugin("java")
//        plugin("com.modrinth.minotaur")
//        plugin("net.darkhax.curseforgegradle")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        withSourcesJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
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

        val isVanilla = projectExt.platform.get() == "Vanilla"
        val isFabric = projectExt.platform.get() == "Fabric"

        tasks.compileJava {
            projectsToInclude.forEach {
                source(it.sourceSets.getByName("main").allSource)
            }
        }

        tasks.compileKotlin  {
            projectsToInclude.forEach {
                source(it.sourceSets.getByName("main").allSource)
            }
        }

        tasks.getByName<Jar>("sourcesJar") {
            projectsToInclude.forEach {
                val mainSourceSet = it.sourceSets.getByName("main")
                from(mainSourceSet.allJava)
            }
        }
        tasks.kotlinSourcesJar {
            projectsToInclude.forEach {
                val mainSourceSet = it.sourceSets.getByName("main")
                from(mainSourceSet.allJava)
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
            archivesName = ("$archivesBaseNameProp-${projectExt.platform.get().lowercase()}-${projectExt.minecraftVersion.get()}")
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