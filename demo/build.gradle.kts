import org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe
import ir.mahozad.manifest.ManifestMode

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)

    // Included (composite) build; see settings.gradle.kts files
    id("ir.mahozad.compose-exe-manifest")
}

composeExeManifest {
    enabled = true
    manifestMode = ManifestMode.EMBED
    manifestFile = file("example.manifest")
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
                version = libs.versions.proguard.get()
            }
        }
    }
}
