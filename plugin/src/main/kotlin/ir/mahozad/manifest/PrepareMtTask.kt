package ir.mahozad.manifest

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class PrepareMtTask : DefaultTask() {

    @get:OutputFile
    val mtExeFile = project.objects.fileProperty().value {
        temporaryDir.resolve("mt.exe")
        // Could also have used the Gradle home directory like this:
        // project
        //     .gradle
        //     .gradleUserHomeDir
        //     .resolve("compose-exe-manifest")
        //     .also(File::mkdir)
        //     .resolve("mt.exe")
    }

    @TaskAction
    fun execute() {
        val mtFile = mtExeFile.get().asFile
        val dllFile = mtFile.resolveSibling("midlrtmd.dll")
        javaClass.getResourceAsStream("/mt_x64/${mtFile.name}")
            ?.use { mtFile.outputStream().use(it::copyTo) }
        javaClass.getResourceAsStream("/mt_x64/${dllFile.name}")
            ?.use { dllFile.outputStream().use(it::copyTo) }
    }
}
