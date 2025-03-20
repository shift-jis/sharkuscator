plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"

    id("buildsrc.convention.kotlin-jvm")
    application
}

dependencies {
    implementation(project(":sharkuscator.obfuscator"))
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
}

application {
    mainClass = "dev.sharkuscator.BootstrapSharkuscator"
}
