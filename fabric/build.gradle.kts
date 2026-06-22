import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("multiloader-loader")

    alias(libs.plugins.loom)
}
val utils = project.utils(versionCatalogs, ext)

val modid: String by project
val minecraftVersion = libs.versions.minecraft.asProvider().get()

loom {
//    splitEnvironmentSourceSets()

    accessWidenerPath = project(COMMON_PROJECT).file("src/main/resources/${modid}.accesswidener")
    mixin.defaultRefmapName = "${modid}.refmap.json"

    runs {
        named("client") {
            configName = "Filloaxlib - Fabric Client"

            client()
            ideConfigGenerated(true)
            runDir("runs/" + name)
            programArg("--username=Dev")
        }

        named("server") {
            configName = "Filloaxlib - Fabric Server"

            server()
            ideConfigGenerated(true)
            runDir("runs/" + name)
        }
    }

    mods {
        register(modid) {
            sourceSet(sourceSets.main.get())
//            sourceSet(sourceSets["client"])
        }
    }
}

fabricApi {
    configureTests {
        createSourceSet = true
        modId = "$modid-test"
        enableGameTests = true
        eula = true
    }
}

val modVersion = libs.versions.modversion.get()
//val parchmentMcVersion = libs.versions.parchment.minecraft.get()
//val parchmentVersion = libs.versions.parchment.asProvider().get()

version = "$modVersion-${minecraftVersion}-fabric"

configurations {
    create(COMMON_GAMETEST_JAVA) { isCanBeResolved = true }
}

tasks.named<KotlinCompile>("compileGametestKotlin") {
    dependsOn(configurations.getByName(COMMON_GAMETEST_JAVA))
    source(configurations.getByName(COMMON_GAMETEST_JAVA))
}

dependencies {
    minecraft( libs.minecraft )
    implementation( libs.jsr305 )
//    mappings(loom.layered {
//        officialMojangMappings()
//        parchment("org.parchmentmc.data:parchment-${parchmentMcVersion}:${parchmentVersion}@zip")
//    })
    implementation( libs.fabric )
    implementation( libs.fabric.api ) {
        exclude(module = "fabric-api-deprecated")
    }
    implementation( libs.fabric.kotlin )

    utils.includeLibs.forEach {
        api(it)
        include(it)
    }

    COMMON_GAMETEST_JAVA(project(path = COMMON_PROJECT, configuration = COMMON_GAMETEST_JAVA))
}