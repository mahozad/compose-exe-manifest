import org.gradle.api.tasks.wrapper.Wrapper.DistributionType

plugins {
    // Using the Plugin Publish plugin makes plugin publication possible.
    // It also in turn auto-applies the following two plugins:
    //   - Gradle Plugin Development Plugin (java-gradle-plugin)
    //   - Maven Publish plugin (maven-publish)
    // See https://plugins.gradle.org/docs/publish-plugin
    alias(libs.plugins.publish.plugin) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}

subprojects {
    repositories {
        mavenCentral()
        google()
    }
}

tasks.wrapper {
    gradleVersion = libs.versions.gradle.get()
    networkTimeout = 60_000 // milliseconds
    distributionType = DistributionType.ALL
    validateDistributionUrl = false
}
