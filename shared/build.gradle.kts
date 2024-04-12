plugins {
    kotlin("jvm")
}

repositories {
    maven(uri("https://libraries.minecraft.net"))
}

dependencies {
    // In shared functions that do not depend directly on minecraft (otherwise included in MC)
    compileOnly("org.apache.logging.log4j:log4j-core:2.19.0")
    compileOnly("com.mojang:datafixerupper:6.0.8")
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    // Included in other version-specific libraries
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.23")
}
