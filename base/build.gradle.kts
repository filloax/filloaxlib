plugins {
    id("multiloader-convention")

    alias(libs.plugins.vanillagradle)
    alias(libs.plugins.kotlinserialization)
}

val modid: String by project
val modVersion = libs.versions.modversion.get()
val minecraftVersion = libs.versions.minecraft.asProvider().get()

version = "$modVersion-${minecraftVersion}-base"

base {
    archivesName = modid
}

minecraft {
    version(minecraftVersion)
    accessWideners(file("src/main/resources/${modid}.accesswidener"))
}

dependencies {
    implementation( libs.jsr305 )

    implementation( libs.kotlin.stdlib )
    implementation( libs.kotlin.reflect )
    implementation( libs.kotlin.serialization )

    compileOnly( libs.mixin )
    compileOnly( libs.mixinextras.common )

    testImplementation( libs.junit )
}

sourceSets.main.get().resources.srcDir(project(":base").file("src/generated/resources"))