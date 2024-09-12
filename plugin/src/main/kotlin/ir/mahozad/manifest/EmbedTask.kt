package ir.mahozad.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction
import java.io.File

// The class is made abstract so that Gradle will handle many things automatically.
abstract class EmbedTask : DefaultTask() {

    init {
        group = "compose desktop" // Same group as CMP tasks
        description = "Embeds a manifest file in the app exe"
    }

    @get:Input
    lateinit var manifestMode: Provider<ManifestMode>

    // It's not necessary to check for existence of the file or if it's a directory.
    // Because Gradle automatically checks those when annotated with @InputFile.
    @get:InputFile
    lateinit var manifestFile: Provider<File>

    @get:InputFile
    lateinit var mtExeFile: Provider<File>

    @get:InputDirectory
    lateinit var exeDirectory: Provider<Directory>

    @get:Optional // For non-Windows OSes that do not create exe file
    @get:OutputFile
    val outputExeFile by lazy {
        // OR to get a Provider could use exeDirectory.map { it.asFileTree }.filter {...}
        exeDirectory.get().asFile.walk().maxDepth(2).firstOrNull { it.extension == "exe" }
    }

    @get:Optional // For when the manifest mode is only embed
    @get:OutputFile
    val outputManifestFile by lazy {
        // OR to get a Provider could use outputExeFile.map { it.resolveSibling("${it.name}.manifest") }
        outputExeFile?.resolveSibling("${outputExeFile?.name}.manifest")
    }

    @TaskAction
    fun execute() {
        outputManifestFile?.delete()
        val exeFile = outputExeFile
            ?: throw StopExecutionException("Did not find exe file")
        if (manifestMode.get().shouldEmbed) {
            exeFile.temporaryWritable(::embedManifestIn)
            logger.info("Embedded manifest in $exeFile")
        } else {
            val manifestName = "${exeFile.name}.manifest"
            val manifestFile = exeFile.resolveSibling(manifestName)
            copyManifestTo(manifestFile)
            logger.info("Copied manifest to $manifestFile")
        }
    }

    private fun File.temporaryWritable(block: (File) -> Unit) {
        setWritable(true)
        block(this)
        setWritable(false)
    }

    private fun copyManifestTo(destination: File) {
        manifestFile
            .get()
            .inputStream()
            .use { destination.outputStream().use(it::copyTo) }
    }

    private fun embedManifestIn(exe: File) {
        ProcessBuilder()
            .command(
                mtExeFile.get().absolutePath,
                "-nologo",
                "-manifest", manifestFile.get().absolutePath,
                "-outputresource:\"${exe.absolutePath};#1\""
            )
            .directory(exe.parentFile)
            .start()
            .inputReader()
            .forEachLine(logger::info)
    }
}
