ext["mcVersion"] = "1.20.6"
ext["platform"] = "Vanilla"
ext["supported"] = listOf("1.20.6")

minecraft {
    version("1.20.4")

    // Used only in dev, in actual mod uses the platform AW
    accessWideners("src/main/resources/fxlib_base_1.20.6.accesswidener")
}

println("")

val mixinVersion: String by project
val mixinExtrasVersion: String by project
val kotlinVersion: String by project
val kotlinxSerializationVersion: String by project

dependencies {
    compileOnly("org.spongepowered:mixin:$mixinVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    compileOnly("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")
}