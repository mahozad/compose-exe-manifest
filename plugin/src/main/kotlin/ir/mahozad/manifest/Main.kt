package ir.mahozad.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import java.io.File
import javax.inject.Inject

// Example plugin: https://github.com/JetBrains/compose-multiplatform/tree/master/gradle-plugins

@Suppress("unused")
abstract class EmbedPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val embedExtension = project.extensions.create(
            "composeExeManifest",
            EmbedExtension::class.java,
            project
        )

        val embedTask = project.tasks.register(
            "embedManifestInExe",
            EmbedTask::class.java,
            embedExtension
        )

        // TO access AbstractJPackageTask, we needed to add the plugin as a dependency in our build file.
        // Could also have used the code below which does not need the dependency
        // on JetBrains Compose plugin in our dependencies {}
        // but it would not even be invoked when the user executed tasks like packageExe.
        // In addition, we need to wrap the code in project.afterEvaluate {} (not recommended) to ensure
        // the search for tasks is executed after the build init and configuration phase has been completed
        // because those JetBrains Compose adds its tasks during or after project init/configuration
        // project.afterEvaluate {
        //     project.tasks
        //         .filter { it.name in setOf("createDistributable", "createReleaseDistributable") }
        //         .forEach { it.finalizedBy(embedManifestInExe) }
        // }
        // FIXME: When user runs packageExe task, our task is executed but has no effect
        //  probably because we don't know where the temporary app exe file is stored before being packaged
        project.tasks.withType(AbstractJPackageTask::class.java) {
            // This block is executed only if there is tasks with type AbstractJPackageTask
            // In other words, this block is skipped if JetBrains Compose plugin has not been applied
            it.finalizedBy(embedTask)
        }
    }
}

abstract class EmbedExtension @Inject constructor(project: Project) {
    /**
     * Whether the embedding is enabled.
     *
     * Defaults to `true`.
     */
    val enabled = project.objects.property<Boolean>(Boolean::class.java).value(true)

    /**
     * The manifest file to embed in app exe.
     * Its content is not validated by the plugin.
     *
     * Defaults to `app.manifest` at the project/module directory.
     */
    val manifestFile = project.objects.fileProperty().value { File("app.manifest") }

    /**
     * Whether to copy the manifest file to where the app exe resides.
     *
     * Defaults to `false`.
     */
    val copyManifestToExeDirectory = project.objects.property(Boolean::class.java).value(false)
}

// It’s beneficial to make the class abstract because Gradle will handle many things automatically.
abstract class EmbedTask @Inject constructor(
    private val pluginConfigs: EmbedExtension
) : DefaultTask() {

    init {
        onlyIf { pluginConfigs.enabled.get() }
        group = "compose desktop"
        description = "Embeds a manifest file in the app exe"
    }

    private val mtFile: File by lazy {
        val mtFile = temporaryDir.resolve("mt.exe")
        val dllFile = mtFile.resolveSibling("midlrtmd.dll")
        javaClass.getResourceAsStream("/mt_x64/${mtFile.name}")
            ?.use { mtFile.outputStream().use(it::copyTo) }
        javaClass.getResourceAsStream("/mt_x64/${dllFile.name}")
            ?.use { dllFile.outputStream().use(it::copyTo) }
        mtFile
    }

    @TaskAction
    fun action() {
        project
            .tasks
            .withType(AbstractJPackageTask::class.java)
            .map { it.destinationDir }
            .map { it.asFile }
            .map { it.get() }
            .filter { it.endsWith("app") }
            .map { it.walk() }
            .map { it.first { it.extension == "exe" } }
            .onEach { logger.info("Embedding manifest in $it") }
            .onEach { it.setWritable(true) } // Ensures the file is not readonly
            .onEach { embedManifestIn(it) }
            .onEach { it.setWritable(false) }
            .takeIf { pluginConfigs.copyManifestToExeDirectory.get() }
            ?.forEach { copyManifestTo(it.resolveSibling("${it.name}.manifest")) }
    }

    private fun copyManifestTo(destination: File) {
        pluginConfigs
            .manifestFile
            .get()
            .asFile
            .takeIf(File::exists)
            ?.takeIf(File::isFile)
            ?.inputStream()
            ?.let { destination.outputStream().use(it::copyTo) }
    }

    private fun embedManifestIn(exe: File) {
        ProcessBuilder()
            .command(
                mtFile.absolutePath,
                "-nologo",
                "-manifest", pluginConfigs.manifestFile.get().asFile.absolutePath,
                "-outputresource:\"${exe.absolutePath};#1\""
            )
            .directory(exe.parentFile)
            .start()
            .inputReader()
            .forEachLine(::println)
    }
}
