import org.gradle.api.internal.provider.AbstractProperty
import org.gradle.api.internal.provider.EvaluationContext
import org.gradle.api.internal.provider.PropertyHost
import org.gradle.api.internal.provider.ValueSupplier
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")

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

/* Unused as idk how to make it work with kotlin
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
val commonProjectName = "shared"

subprojects {
    apply {
        plugin("java")
//        plugin("com.modrinth.minotaur")
//        plugin("net.darkhax.curseforgegradle")
    }

//    extensions.create("platformInfo", PlatformInfoExtension::class.java)

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

    base {
        archivesName.set(archivesBaseNameProp)
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

    afterEvaluate {
//        val projectExt = extensions.findByType<PlatformInfoExtension>()
//            ?: throw Exception("No projectExt found for $this")
        val projectExt = PlatformInfo(ext)

        dependencies {
            implementation(project(":$commonProjectName"))

            if (!name.endsWith("base")) {
                val baseProject = ":${name.substringBefore('-')}-base"
                implementation(project(baseProject))
            }
        }

//        println("Setup dependencies for $name: are ${project.configurations.map { "\n\t${it.name}: ${it.dependencies.map {d -> d.name}}" } }")

        val useLoom = extensions.findByName("loom")

        if (useLoom != null) {
            // Configure Loom if needed
        }

        base {
            archivesName.apply { set(get() + "-${projectExt.platform.get()}-${projectExt.minecraftVersion.get()}") }
        }

//            if (projectExt.platform.get() == "Vanilla") {
//                tasks.named("modrinth") {
//                    enabled = false
//                }
//            } else {
//                tasks.register("curseforge", TaskPublishCurseForge::class) {
//                    disableVersionDetection()
//                    apiToken = System.getenv("CURSEFORGE_TOKEN")
//                    val projectId = System.getenv("CURSEFORGE_PROJECT_ID")
//                    val mainFile = upload(projectId, if (useLoom != null) remapJar else jar)
//                    mainFile.addModLoader(projectExt.platform.get())
//                    projectExt.supportedMinecraftVersions.getOrElse(emptyList()).forEach {
//                        mainFile.addGameVersion(it)
//                    }
//                    mainFile.releaseType = "release"
//                    mainFile.changelog = "Bug fixes"
//                }
//            }

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