plugins {
    id("multiloader-convention")

    alias(libs.plugins.minivan)
    alias(libs.plugins.kotlinserialization)
}

val modid: String by project
val modVersion = libs.versions.modversion.get()
val minecraftVersion = libs.versions.minecraft.asProvider().get()

version = "$modVersion-${minecraftVersion}-common"

base {
    archivesName = modid
}

val mc = minivan.minecraftBuilder()
    .version(minecraftVersion)
    .accessWideners("src/main/resources/${modid}.accesswidener")
    .build()
    .minecraft;

dependencies {
    implementation( libs.jsr305 )
    implementation( libs.log4j )

    compileOnly( libs.jetbrains.annotations )

    implementation( libs.kotlin.stdlib )
    implementation( libs.kotlin.reflect )
    implementation( libs.kotlin.serialization )

    compileOnly( libs.mixin )
    compileOnly( libs.mixinextras.common )

    testImplementation( libs.junit )
    testImplementation( libs.gson )
}

project.dependencies.add("compileOnly", project.files(mc.minecraft))
mc.dependencies.forEach { project.dependencies.add("compileOnly", it) }

sourceSets.main.get().resources.srcDir(project(":base").file("src/generated/resources"))

// Test
tasks.test {
    useJUnitPlatform()
}
// Fix minivan not adding minecraft to test classpath
project.dependencies.add("testImplementation", project.files(mc.minecraft))
mc.dependencies.forEach { project.dependencies.add("testImplementation", it) }
