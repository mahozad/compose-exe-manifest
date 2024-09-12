pluginManagement {
    // Treats the plugin as a separate project instead of include()ing it as a subproject
    //  so that there is no need to publish the plugin to mavenLocal to test every change
    includeBuild("plugin")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        // mavenLocal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "compose-exe-manifest"

include("demo")
// See pluginManagement block
// include("plugin")
