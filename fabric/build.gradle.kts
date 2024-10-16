plugins {
    id("multiloader-convention")

    alias(libs.plugins.loom)
    alias(libs.plugins.kotlinserialization)
}

val modid: String by project
val minecraftVersion = libs.versions.minecraft.asProvider().get()

loom {
//    splitEnvironmentSourceSets()

    accessWidenerPath = project(":base").file("src/main/resources/${modid}.accesswidener")
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

base {
    archivesName = modid
}


val baseProject = project(":base")

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

    includeLibs.forEach {
        api(it)
        include(it)
    }

    compileOnly(baseProject)
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

tasks.withType<Javadoc>().configureEach {
    source(baseProject.sourceSets.getByName("main").allJava)
}

tasks.processResources {
    from(baseProject.sourceSets.getByName("main").resources)
}

publishing {
    publications {
        create<MavenPublication>("${modid}-fabric") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = this.name
            version = "$modVersion-$minecraftVersion"
        }
    }
}