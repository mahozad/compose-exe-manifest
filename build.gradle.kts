import org.gradle.api.tasks.wrapper.Wrapper.DistributionType

plugins {
    // Applies the Plugin Publish plugin to make plugin publication possible.
    // It also in turn auto-applies the following two plugins:
    //   - Gradle Plugin Development Plugin (java-gradle-plugin)
    //   - Maven Publish plugin (maven-publish)
    // See https://plugins.gradle.org/docs/publish-plugin
    id("com.gradle.plugin-publish") version "1.2.2" apply false
    id("org.jetbrains.kotlin.jvm") version "2.0.20" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
}

subprojects {
    repositories {
        mavenCentral()
        google()
    }
}

tasks.wrapper {
    gradleVersion = "8.10"
    networkTimeout = 60_000 // milliseconds
    distributionType = DistributionType.ALL
    validateDistributionUrl = false
}
