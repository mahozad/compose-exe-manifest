package ir.mahozad.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.extensions.stdlib.capitalized
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

        // To access AbstractJPackageTask, we needed to add CMP plugin as a dependency in our build file.
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
        project.tasks.withType(AbstractJPackageTask::class.java) { composePackagingTask ->
            // This block is executed only if there is tasks with type AbstractJPackageTask
            // In other words, this block is skipped if JetBrains Compose plugin has not been applied

            // FIXME: When user runs CMP packageExe task, our task is executed but has no effect
            //  probably because we don't know where the temporary app exe file is stored before being packaged
            if ("package" in composePackagingTask.name) return@withType

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

abstract class EmbedExtension @Inject constructor(project: Project) {
    /**
     * Whether the embedding/copying is enabled.
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
     * Whether to embed the manifest in the app exe or to copy it to the app exe directory or both.
     *
     * Defaults to [ManifestMode.EMBED].
     */
    val manifestMode = project.objects.property(ManifestMode::class.java).value(ManifestMode.EMBED)
}

@Suppress("unused")
/**
 * Whether to embed the manifest in the app exe or to copy it to the app exe directory or both.
 */
enum class ManifestMode {
    /**
     * Embeds the manifest file in the app exe.
     */
    EMBED,

    /**
     * Copies the manifest file to the app exe directory.
     */
    COPY,

    /**
     * Copies the manifest file to the app exe directory and also embeds the manifest in the app exe.
     */
    COPY_AND_EMBED;

    val shouldEmbed get() = this == EMBED || this == COPY_AND_EMBED
}

// The class is made abstract so that Gradle will handle many things automatically.
abstract class EmbedTask : DefaultTask() {

    init {
        group = "compose desktop"
        description = "Embeds a manifest file in the app exe"
    }

    @get:Input
    lateinit var manifestMode: Provider<ManifestMode>

    @get:InputFile
    // It's not needed to check for existence of the file or if it's a directory.
    // Because it has been declared as an @InputFile and Gradle automatically does that.
    lateinit var manifestFile: Provider<File>

    @get:InputDirectory
    lateinit var exeDirectory: Provider<Directory>

    private val mtExe: File by lazy {
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
        val exeFile = exeDirectory
            .get()
            .asFile
            .walk()
            .firstOrNull { it.extension == "exe" }
            ?: return
        if (manifestMode.get().shouldEmbed) {
            logger.info("Embedding manifest in $exeFile")
            exeFile.setWritable(true) // Ensures the file is not readonly
            embedManifestIn(exeFile)
            exeFile.setWritable(false)
        } else {
            val manifestName = "${exeFile.name}.manifest"
            logger.info("Copying manifest as $manifestName")
            copyManifestTo(exeFile.resolveSibling(manifestName))
        }
    }

    private fun copyManifestTo(destination: File) {
        manifestFile
            .get()
            .inputStream()
            .let { destination.outputStream().use(it::copyTo) }
    }

    private fun embedManifestIn(exe: File) {
        ProcessBuilder()
            .command(
                mtExe.absolutePath,
                "-nologo",
                "-manifest", manifestFile.get().absolutePath,
                "-outputresource:\"${exe.absolutePath};#1\""
            )
            .directory(exe.parentFile)
            .start()
            .inputReader()
            .forEachLine(::println)
    }
}
