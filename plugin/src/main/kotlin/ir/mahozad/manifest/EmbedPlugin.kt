package ir.mahozad.manifest

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask

@Suppress("unused")
abstract class EmbedPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val embedExtension = project.extensions.create(
            "composeExeManifest",
            EmbedExtension::class.java,
            project
        )

        // To access AbstractJPackageTask, we needed to add CMP plugin as a dependency in our build file.
        //
        // Instead of using Kotlin filter, map, forEach, etc. the Gradle withType, matching, all etc. are used
        // which are live and execute their actions when new tasks are added later.
        // Also, this block is executed only if there is tasks with type AbstractJPackageTask
        // In other words, this block is skipped if JetBrains Compose plugin has not been applied
        //
        // Could also have used the code below which does not need the dependency on CMP plugin in our dependencies{}.
        // But we need to wrap the code in project.afterEvaluate{} (not recommended) to ensure
        // the search for tasks is executed after the build init and configuration phase has been completed
        // because CMP plugin adds its tasks during or after project init/configuration.
        // project.afterEvaluate {
        //     project.tasks
        //         .filter { it.name in setOf("createDistributable", "createReleaseDistributable") }
        //         .forEach { it.finalizedBy(embedManifestInExe) }
        // }
        project
            .tasks
            .withType(AbstractJPackageTask::class.java)
            // FIXME: When user runs CMP packageExe task, our task is executed but has no effect
            //  probably because we don't know where the temporary app exe file is stored before being packaged
            .matching { "package" !in it.name }
            .all { composePackagingTask ->
                val embedTask = project.tasks.register(
                    "embedManifestInExeFor${composePackagingTask.name.capitalized()}",
                    EmbedTask::class.java,
                ) {
                    it.enabled = embedExtension.enabled.get()
                    it.manifestMode = embedExtension.manifestMode
                    it.manifestFile = embedExtension.manifestFile.asFile
                    it.exeDirectory = composePackagingTask.destinationDir
                }
                composePackagingTask.finalizedBy(embedTask)
            }
    }
}
