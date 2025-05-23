import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"

    id("io.freefair.lombok") version "8.10"
    kotlin("plugin.lombok") version "2.1.10"

    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(files("../thirdparty/mapleir-full-with-deps.jar"))
    implementation(project(":sharkuscator.annotations"))

    implementation("net.java.dev.jna:jna-platform:5.17.0")
    implementation("net.java.dev.jna:jna:5.17.0")

    implementation("org.apache.logging.log4j:log4j-core:3.0.0-beta3")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("meteordevelopment:orbit:0.2.4")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.javaPhantom)
    implementation(libs.koffee)

    testImplementation(kotlin("test"))
}

tasks.named("shadowJar", ShadowJar::class.java) {
    archiveFileName = "sharkuscator.jar"
}
