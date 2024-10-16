plugins {
    id("multiloader-convention")

    alias(libs.plugins.moddevgradle)
    alias(libs.plugins.kotlinserialization)
}

val modid: String by project
val modVersion = libs.versions.modversion.get()
val minecraftVersion = libs.versions.minecraft.asProvider().get()

version = "$modVersion-${minecraftVersion}-neoforge"

repositories {
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

base {
    archivesName = modid
}

val baseProject = project(":base")

neoForge {
    version.set(libs.versions.neoforge.asProvider())

    validateAccessTransformers = true
    accessTransformers.files.setFrom(baseProject.file("src/main/resources/META-INF/accesstransformer.cfg"))

    parchment {
        minecraftVersion = libs.versions.parchment.minecraft
        mappingsVersion = libs.versions.parchment.asProvider()
    }

    runs {
        create("client") {
            client()

            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            systemProperty("neoforge.enabledGameTestNamespaces", modid)
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", modid)
        }

        // This run config launches GameTestServer and runs all registered gametests, then exits.
        // By default, the server will crash when no gametests are provided.
        // The gametest system is also enabled by default for other run configs under the /test command.
        create("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", modid)
        }

        create("data") {
            data()

            // example of overriding the workingDirectory set in configureEach above, uncomment if you want to use it
            // gameDirectory = project.file('run-data')

            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            programArguments.addAll( "--mod", modid, "--all", "--output", file("src/generated/resources/").absolutePath, "--existing", file("src/main/resources/").absolutePath)
        }

        // applies to all the run configs above
        configureEach {
            // Recommended logging data for a userdev environment
            // The markers can be added/remove as needed separated by commas.
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            systemProperty("forge.logging.markers", "REGISTRIES")

            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        register(modid) {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    implementation( libs.kotlinforge )
    implementation( libs.kotlin.serialization )

    includeLibs.forEach {
        api(it)
        jarJar(it)
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
        create<MavenPublication>("${modid}-neoforge") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = this.name
            version = "$modVersion-$minecraftVersion"
        }
    }
}
