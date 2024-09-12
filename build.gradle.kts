import org.gradle.api.tasks.wrapper.Wrapper.DistributionType

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
