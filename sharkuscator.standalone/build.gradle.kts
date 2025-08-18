import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"

    id("buildsrc.convention.kotlin-jvm")
    application
}

version = "1.0.0"

dependencies {
    implementation(project(":sharkuscator.obfuscator"))
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
}

application {
    mainClass = "dev.sharkuscator.BootstrapSharkuscator"
}

tasks.named("shadowJar", ShadowJar::class.java) {
    archiveFileName = "sharkuscator-${version}.jar"
}
