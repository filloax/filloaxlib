plugins {
    id("multiloader-loader")

    alias(libs.plugins.moddevgradle)
}
val utils = project.utils(versionCatalogs, ext)

val modid: String by project
val modVersion = libs.versions.modversion.get()
val minecraftVersion = libs.versions.minecraft.asProvider().get()

version = "$modVersion-${minecraftVersion}-neoforge"


val baseProject = project(COMMON_PROJECT)

neoForge {
    version = libs.versions.neoforge.asProvider().get()

    validateAccessTransformers = true
    accessTransformers.files.setFrom(baseProject.file("src/main/resources/META-INF/accesstransformer.cfg"))

    parchment {
        minecraftVersion = libs.versions.parchment.minecraft
        mappingsVersion = libs.versions.parchment.asProvider()
    }

    runs {
        create("client") {
            client()
        }

        create("server") {
            server()
        }

        create("gameTestServer") {
            type = "gameTestServer"
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("neoforge.enabledGameTestNamespaces", modid)

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

    utils.includeLibs.forEach {
        api(it)
        jarJar(it)
    }
}