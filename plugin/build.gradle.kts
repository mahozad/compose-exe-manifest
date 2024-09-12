plugins {
    // `kotlin-dsl`
    alias(libs.plugins.publish.plugin)
    alias(libs.plugins.kotlin.jvm)
}

group = "ir.mahozad"
version = "0.6.0"

dependencies {
    // Found the id of the plugin as described in https://stackoverflow.com/q/74221701
    // https://mvnrepository.com/artifact/org.jetbrains.compose/compose-gradle-plugin/1.6.11
    //
    // compileOnly scope is used so the Compose Multiplatform dependency version
    // is not overridden for the project of user when they apply our plugin
    compileOnly(libs.compose.multiplatform.plugin)
    // testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("compose-exe-manifest") {
            id = "ir.mahozad.compose-exe-manifest"
            implementationClass = "ir.mahozad.manifest.EmbedPlugin"
            description = "Embeds application manifest XML file in Compose Multiplatform desktop exe file"
            displayName = "Compose Exe Manifest"
            website = "https://github.com/mahozad/compose-exe-manifest"
            vcsUrl = "https://github.com/mahozad/compose-exe-manifest.git"
            tags = listOf(
                "compose-multiplatform",
                "application-manifest",
                "manifest",
                "exe"
            )
        }
    }
}
