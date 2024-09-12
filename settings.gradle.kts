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

rootProject.name = "compose-exe-manifest"

include("demo")
// See pluginManagement block
// include("plugin")
