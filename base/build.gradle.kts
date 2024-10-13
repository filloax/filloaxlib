plugins {
    id("multiloader-convention")

    alias(libs.plugins.moddevgradle)
    alias(libs.plugins.kotlinserialization)
}

val modid: String by project
val modVersion = libs.versions.modversion.get()
val minecraftVersion = libs.versions.minecraft.asProvider().get()

version = "$modVersion-${minecraftVersion}-common"

base {
    archivesName = modid
}

neoForge {
    neoFormVersion = libs.versions.neoform

    validateAccessTransformers = true

    parchment {
        minecraftVersion = libs.versions.parchment.minecraft
        mappingsVersion = libs.versions.parchment.asProvider()
    }

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
    implementation( libs.jsr305 )
    implementation( libs.log4j )

    implementation( libs.kotlin.stdlib )
    implementation( libs.kotlin.reflect )
    implementation( libs.kotlin.serialization )

    compileOnly( libs.mixin )
    compileOnly( libs.mixinextras.common )

    testImplementation( libs.junit.jupiter )
    testImplementation( libs.gson )
    testRuntimeOnly( libs.junit.launcher )
}

sourceSets.main.get().resources.srcDir(project(":base").file("src/generated/resources"))

// Test
tasks.test {
    useJUnitPlatform()
}


publishing {
    publications {
        create<MavenPublication>("${modid}-common") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = this.name
            version = "$modVersion-$minecraftVersion"
        }
    }
}
