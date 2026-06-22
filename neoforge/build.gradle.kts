import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("multiloader-loader")

    alias(libs.plugins.moddevgradle)
}
val utils = project.utils(versionCatalogs, ext)

val modid: String by project
val modVersion = libs.versions.modversion.get()
val minecraftVersion = libs.versions.minecraft.asProvider().get()

version = "$modVersion-${minecraftVersion}-neoforge"

val gametest: SourceSet = sourceSets.create("gametest") {
    compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
}


val baseProject = project(COMMON_PROJECT)

neoForge {
    version = libs.versions.neoforge.asProvider().get()

    validateAccessTransformers = true
    accessTransformers.files.setFrom(baseProject.file("src/main/resources/META-INF/accesstransformer.cfg"))

//    parchment {
//        minecraftVersion = libs.versions.parchment.minecraft
//        mappingsVersion = libs.versions.parchment.asProvider()
//    }

    runs {
        create("client") {
            client()
            ideName.set("Filloaxlib - NeoForge Client")
        }

        create("server") {
            server()
            ideName.set("Filloaxlib - NeoForge Server")
        }

        create("gameTestServer") {
            type = "gameTestServer"
            ideName.set("Filloaxlib - Game Test Server")
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
            sourceSet(sourceSets["gametest"])
        }
    }
}

configurations {
    create(COMMON_GAMETEST_JAVA) { isCanBeResolved = true }
}

tasks.named<KotlinCompile>("compileGametestKotlin") {
    dependsOn(configurations.getByName(COMMON_GAMETEST_JAVA))
    source(configurations.getByName(COMMON_GAMETEST_JAVA))
}

dependencies {
    implementation( libs.kotlinforge )

    utils.includeLibs.forEach {
        api(it)
        jarJar(it)
    }

    COMMON_GAMETEST_JAVA(project(path = COMMON_PROJECT, configuration = COMMON_GAMETEST_JAVA))
}