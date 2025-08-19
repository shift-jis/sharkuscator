import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(project(":sharkuscator.annotations"))
    implementation(project(":sharkuscator.commons"))

    implementation("net.java.dev.jna:jna-platform:5.17.0")
    implementation("net.java.dev.jna:jna:5.17.0")

    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.github.Col-E:jphantom:1.4.4")
    implementation("meteordevelopment:orbit:0.2.4")

    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("ch.qos.logback:logback-core:1.5.18")

    implementation(libs.bundles.kotlinxEcosystem)
    testImplementation(kotlin("test"))
}

tasks.named("shadowJar", ShadowJar::class.java) {
    archiveFileName = "sharkuscator.jar"
}
