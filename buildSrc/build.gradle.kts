plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.plugin.kotlin.jvm)
    implementation(libs.plugin.kotlin.serialization)
    implementation(libs.plugin.dokka)
}