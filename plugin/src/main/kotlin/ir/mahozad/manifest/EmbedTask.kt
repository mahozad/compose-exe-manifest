package ir.mahozad.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

// The class is made abstract so that Gradle will handle many things automatically.
abstract class EmbedTask : DefaultTask() {

    init {
        group = "compose desktop" // Same group as CMP tasks
        description = "Embeds a manifest file in the app exe"
    }

    // It's not needed to check for existence of the file or if it's a directory.
    // Because it has been declared as an @InputFile and Gradle automatically does that.
    @get:InputFile
    lateinit var manifestFile: Provider<File>

    @get:Input
    lateinit var manifestMode: Provider<ManifestMode>

    @get:InputDirectory
    lateinit var exeDirectory: Provider<Directory>

    @get:OutputFile
    val outputExeFile by lazy {
        // OR to get a Provider could use exeDirectory.map { it.asFileTree }.map { ... }
        exeDirectory.get().asFile.walk().firstOrNull { it.extension == "exe" }
    }

    @get:Optional
    @get:OutputFile
    val outputManifestFile by lazy {
        // OR to get a Provider could use outputExeFile.map { it.resolveSibling("${it.name}.manifest") }
        outputExeFile?.resolveSibling("${outputExeFile?.name}.manifest")
    }

    private val mtExe: File by lazy {
        val mtFile = temporaryDir.resolve("mt.exe")
        val dllFile = mtFile.resolveSibling("midlrtmd.dll")
        javaClass.getResourceAsStream("/mt_x64/${mtFile.name}")
            ?.use { mtFile.outputStream().use(it::copyTo) }
        javaClass.getResourceAsStream("/mt_x64/${dllFile.name}")
            ?.use { dllFile.outputStream().use(it::copyTo) }
        return@lazy mtFile
    }

    @TaskAction
    fun execute() {
        outputManifestFile?.delete()
        val exeFile = outputExeFile ?: return
        if (manifestMode.get().shouldEmbed) {
            exeFile.temporaryWritable(::embedManifestIn)
            logger.info("Embedded manifest in $exeFile")
        } else {
            val manifestName = "${exeFile.name}.manifest"
            val manifestFile = exeFile.resolveSibling(manifestName)
            copyManifestTo(manifestFile)
            logger.info("Copying manifest to $manifestFile")
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