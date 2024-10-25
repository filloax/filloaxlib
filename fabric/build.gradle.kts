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
            configName = "Fabric Client"

            client()
            ideConfigGenerated(true)
            runDir("runs/" + name)
            programArg("--username=Dev")
        }

        named("server") {
            configName = "Fabric Server"

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

val modVersion = libs.versions.modversion.get()
val parchmentMcVersion = libs.versions.parchment.minecraft.get()
val parchmentVersion = libs.versions.parchment.asProvider().get()

version = "$modVersion-${minecraftVersion}-fabric"

dependencies {
    minecraft( libs.minecraft )
    implementation( libs.jsr305 )
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchmentMcVersion}:${parchmentVersion}@zip")
    })
    modImplementation( libs.fabric )
    modImplementation( libs.fabric.api ) {
        exclude(module = "fabric-api-deprecated")
    }
    modImplementation( libs.fabric.kotlin )

    utils.includeLibs.forEach {
        api(it)
        include(it)
    }
}