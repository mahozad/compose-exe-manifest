package ir.mahozad.manifest

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.toPath
import kotlin.io.path.writeText

class ExampleTest {

    @Disabled
    @Test
    fun greeterPluginAddsGreetingTaskToProject() {
        val project = ProjectBuilder.builder().build()
        // project.pluginManager.apply("org.jetbrains.compose")
        project.pluginManager.apply("ir.mahozad.compose-exe-manifest")
        assertThat(project.tasks.getByName("embedManifestForCreateDistributable"))
            .isInstanceOf(EmbedTask::class.java)
    }

    @Disabled
    @Test
    fun `When the Compose-Multiplatform plugin has not been applied, should have no effect`(
        @TempDir tempDir: Path
    ) {
        // var pluginDirectory = javaClass.getResource("/placeholder.txt")?.toURI()?.toPath()?.parent
        var pluginDirectory = javaClass.getProtectionDomain().codeSource.location.toURI().toPath().parent
        while (pluginDirectory?.listDirectoryEntries()?.singleOrNull { it.name == "build.gradle.kts" } == null) {
            pluginDirectory = pluginDirectory?.parent
        }

        val settingsContent = """
            rootProject.name = "Test"
            pluginManagement {
                includeBuild("${pluginDirectory.parent.absolutePathString().replace("\\", "\\\\")}")
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                }
            }
        """
        /*language=kotlin*/
        val buildContent = """
            plugins {
                id("org.jetbrains.compose") version "1.6.11"
                id("ir.mahozad.compose-exe-manifest")
            }
        """
        (tempDir / "settings.gradle.kts").writeText(settingsContent)
        (tempDir / "build.gradle.kts").writeText(buildContent)

        val buildResult = GradleRunner
            .create()
            .withProjectDir(tempDir.toFile())
            .withArguments("--stacktrace")
            .build()

        // assertThat(buildResult.output).contains("Hello world!")
        assertThat(buildResult.task("build")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
