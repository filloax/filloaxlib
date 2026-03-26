plugins {
    id("multiloader-convention")

    alias(libs.plugins.moddevgradle)
}
val utils = project.utils(versionCatalogs, ext)

val modid: String by project
val modVersion = libs.versions.modversion.get()
val minecraftVersion = libs.versions.minecraft.asProvider().get()

version = "$modVersion-${minecraftVersion}-common"

neoForge {
    neoFormVersion = libs.versions.neoform.get()

    validateAccessTransformers = true

//    parchment {
//        minecraftVersion = libs.versions.parchment.minecraft
//        mappingsVersion = libs.versions.parchment.asProvider()
//    }

    mods {
        register(modid) {
            sourceSet(sourceSets.main.get())
        }
    }

    // currently broken https://github.com/neoforged/ModDevGradle/issues/171
//    unitTest {
//        enable()
//        testedMod = mods.getByName(modid)
//    }

    // access transformers use default path so no need to config
}

dependencies {
    compileOnly( libs.jsr305 )
    compileOnly( libs.log4j )

    compileOnly( libs.kotlin.stdlib )
    compileOnly( libs.kotlin.reflect )
    compileOnly( libs.kotlin.serialization )

    compileOnly( libs.mixin )
    compileOnly( libs.mixinextras.common )

    utils.includeLibs.forEach { api(it) }

    testCompileOnly( libs.junit.jupiter )
    testCompileOnly( libs.gson )
    testRuntimeOnly( libs.junit.launcher )
}

sourceSets.main.get().resources.srcDir(project.file("src/generated/resources"))

configurations {
    create(COMMON_JAVA) {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    create(COMMON_RESOURCES) {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    sourceSets.main.get().java.sourceDirectories.forEach { add(COMMON_JAVA, it) }
    sourceSets.main.get().kotlin.sourceDirectories.forEach { add(COMMON_JAVA, it) }
    sourceSets.main.get().resources.sourceDirectories.forEach { add(COMMON_RESOURCES, it) }
}

// Test
tasks.test {
    failOnNoDiscoveredTests = false
    useJUnitPlatform()
}