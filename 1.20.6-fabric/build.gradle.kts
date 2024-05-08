import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

ext["mcVersion"] = "1.20.6"
ext["platform"] = "Fabric"
ext["supported"] = listOf("1.20.6")


loom {
    splitEnvironmentSourceSets()

    mods {
        register("fxlib") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
    }

    accessWidenerPath = file("src/main/resources/fxlib.accesswidener")
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

loom.runs.matching{ it.name != "data" }.configureEach {
    this.vmArg("-Dmixin.debug.export=true")
}