dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.meteordev.org/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("sharkuscator.annotations")
include("sharkuscator.obfuscator")
include("sharkuscator.standalone")

rootProject.name = "sharkuscator"
