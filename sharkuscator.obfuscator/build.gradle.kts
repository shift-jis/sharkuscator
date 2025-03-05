plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.xxDark:SSVM:1.9.0.3")

    implementation(libs.bundles.kotlinxEcosystem)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(8)
}
