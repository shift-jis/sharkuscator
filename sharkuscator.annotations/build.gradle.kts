plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
}
