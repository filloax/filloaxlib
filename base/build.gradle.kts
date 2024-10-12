plugins {
    id("multiloader-convention")

    alias(libs.plugins.vanillagradle)
    alias(libs.plugins.kotlinserialization)
}

val modid: String by project
val modVersion = libs.versions.modversion.get()
val minecraftVersion = libs.versions.minecraft.asProvider().get()

version = "$modVersion-${minecraftVersion}-common"

base {
    archivesName = modid
}

minecraft {
    version(minecraftVersion)
    accessWideners(file("src/main/resources/${modid}.accesswidener"))
}

dependencies {
    implementation( libs.jsr305 )
    implementation( libs.log4j )

    implementation( libs.kotlin.stdlib )
    implementation( libs.kotlin.reflect )
    implementation( libs.kotlin.serialization )

    compileOnly( libs.mixin )
    compileOnly( libs.mixinextras.common )

    testImplementation( libs.junit )
    testImplementation( libs.gson )
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
