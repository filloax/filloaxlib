plugins {
    kotlin("jvm")
}

repositories {
    maven(uri("https://libraries.minecraft.net"))
}

dependencies {
    // In shared functions that do not depend directly on minecraft (otherwise included in MC)
    compileOnly("org.apache.logging.log4j:log4j-core:2.19.0")
    compileOnly("com.mojang:datafixerupper:7.0.14")
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    // Included in other version-specific libraries
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect:1.9.23")
}
