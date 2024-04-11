plugins {
    kotlin("jvm")
}

repositories {
    maven(uri("https://libraries.minecraft.net"))
}

dependencies {
    // In shared functions that do not depend directly on minecraft (otherwise included in MC)
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("com.mojang:datafixerupper:6.0.8")
    implementation("com.google.code.gson:gson:2.10.1")
    // Included in other version-specific libraries
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.23")
}