package ir.mahozad.manifest

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import org.jetbrains.compose.desktop.application.tasks.AbstractRunDistributableTask

// An easy and informative way to debug the tasks and why they are up to date or not is to add
// --info to the gradle command so that it shows the info logs (such as why a task was skipped)

@Suppress("unused")
abstract class EmbedPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val embedExtension = project.extensions.create(
            "composeExeManifest",
            EmbedExtension::class.java
        )

        val prepareMtTask = project.tasks.register(
            "prepareMtExeFile",
            PrepareMtTask::class.java
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
            // FIXME: When user runs CMP packageExe task, our task is executed but has no effect on the exe in package;
            //  it's probably because the jPackage directly creates the app packaged installer
            //  (instead of creating app image and then packaging it into installer).
            //  See the following:
            //  - https://github.com/JetBrains/compose-multiplatform/issues/794
            //  - https://github.com/JetBrains/compose-multiplatform/issues/1972
            //  - https://github.com/JetBrains/compose-multiplatform/issues/2335
            //  - https://github.com/JetBrains/compose-multiplatform/blob/release/1.6.11/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/desktop/application/internal/configureJvmApplication.kt
            //  - https://github.com/JetBrains/compose-multiplatform/blob/release/1.6.11/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/desktop/application/tasks/AbstractJPackageTask.kt
            .matching { "package" !in it.name }
            .all { composePackagingTask ->
                val embedTask = project.tasks.register(
                    "embedManifestInExeFor${composePackagingTask.name.capitalized()}",
                    EmbedTask::class.java
                ) {
                    it.enabled = embedExtension.enabled.get()
                    it.manifestMode = embedExtension.manifestMode
                    it.manifestFile = embedExtension.manifestFile.asFile
                    it.exeDirectory = composePackagingTask.destinationDir
                    it.mtExecutable = prepareMtTask.get().mtExeFile.asFile
                }
                composePackagingTask.finalizedBy(embedTask)
            }

        // TODO: Fix this ugly code
        // This is to prevent Gradle from complaining when executing run*Distributable tasks
        project
            .tasks
            .withType(AbstractRunDistributableTask::class.java)
            .all {
                val prefix = if ("release" in it.name.lowercase()) "Release" else ""
                it.mustRunAfter("embedManifestInExeForCreate${prefix}Distributable")
            }
    }
}
