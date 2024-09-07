import org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("ir.mahozad.compose-exe-manifest") version "0.1.0"
}

composeExeManifest {
    manifestFile = file("example.manifest")
    copyManifestToExeDirectory = false
}

dependencies {
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(Exe)
            packageVersion = "1.0.0"
            packageName = project.name
            buildTypes.release.proguard {
                version = "7.5.0"
            }
        }
    }
}
