plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
}

dependencies {
    implementation(project(":sharkuscator.obfuscator"))
}

application {
    mainClass = "dev.sharkuscator.BootstrapSharkuscator"
}
