plugins {
    kotlin("plugin.lombok") version "2.1.10"
    id("io.freefair.lombok") version "8.10"
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.LLVM-but-worse.maple-ir:app-services:1.0.0-SNAPSHOT-1") {
        exclude(module = "property-framework")
    }
    implementation("com.github.LLVM-but-worse.maple-ir:ir:1.0.0-SNAPSHOT-1") {
        exclude(module = "property-framework")
    }
    implementation("com.github.xxDark:SSVM:1.9.0.3")

    implementation("org.slf4j:slf4j-simple:2.1.0-alpha1")
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    implementation("com.google.code.gson:gson:2.12.1")

    implementation("dev.reimer:progressbar-ktx:0.1.0")
    implementation("me.tongfei:progressbar:0.10.1")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    implementation(libs.bundles.kotlinxEcosystem)
    testImplementation(kotlin("test"))
}

