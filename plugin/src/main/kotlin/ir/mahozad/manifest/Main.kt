package ir.mahozad.manifest

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import java.io.File
import javax.inject.Inject
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import kotlin.io.path.div
import kotlin.io.path.name
import kotlin.io.path.outputStream


// See https://github.com/JetBrains/compose-multiplatform/tree/master/gradle-plugins
// and https://stackoverflow.com/q/78466325/8583692
// and https://stackoverflow.com/q/39493502/8583692


abstract class ExeManifest @Inject constructor(project: Project) {
    /**
     * Whether the embedding is enabled.
     *
     * Defaults to `true`.
     */
    val enabled = project.objects.property<Boolean>(Boolean::class.java).value(true)

    /**
     * The manifest file. It is not validated by the plugin.
     *
     * Defaults to `app.manifest` at the project/module directory.
     */
    var manifestFile = project.file("app.manifest")
    // val file = project.objects.fileProperty().value { File("manifest.xml") }

    /**
     * Whether to copy the manifest beside the exe file.
     *
     * Defaults to `false`.
     */
    val copyManifestToExeDirectory = project.objects.property(Boolean::class.java).value(false)
}

@Suppress("unused")
abstract class ComposeExeManifest : Plugin<Project> {
    override fun apply(project: Project) {
        val composeExeManifest = project.extensions.create(
            "composeExeManifest",
            ExeManifest::class.java,
            project
        )
        val embedManifestInExe = project.tasks.register("embedManifestInExe"/*, Exec::class.java*/) {
            it.doLast {
                if (!composeExeManifest.enabled.get()) return@doLast

                val appExeFiles = project
                    .tasks
                    .filter { it.name in setOf("createDistributable", "createReleaseDistributable") }
                    .map { it.outputs }
                    .map { it.files }
                    .flatMap { it.files }
                    .map { it.walk() }
                    .flatMap { it.filter { it.extension.equals("exe", ignoreCase = true) } }

                // Copies the files from plugin JAR to a directory
                val mtPath = createTempDirectory() / "mt.exe"
                val dllPath = mtPath.resolveSibling("midlrtmd.dll")
                javaClass.getResourceAsStream("/mt_x64/${mtPath.name}")
                    ?.use { mtPath.outputStream().use(it::copyTo) }
                javaClass.getResourceAsStream("/mt_x64/${dllPath.name}")
                    ?.use { dllPath.outputStream().use(it::copyTo) }

                appExeFiles
                    // TODO: Use Gradle logger instead of println
                    .onEach { println("Embedding manifest in $it") }
                    .forEach { appExe ->
                        // Makes the file from readonly to writable
                        appExe.setWritable(true)
                        ProcessBuilder()
                            .command(
                                mtPath.absolutePathString(),
                                "-nologo",
                                "-manifest", composeExeManifest.manifestFile.absolutePath,
                                "-outputresource:\"${appExe.absolutePath};#1\""
                            )
                            .directory(appExe.parentFile)
                            .start()
                            .inputReader()
                            .forEachLine(::println)
                    }

                appExeFiles
                    .onEach { it.setWritable(false) }
                    .takeIf { composeExeManifest.copyManifestToExeDirectory.get() }
                    ?.forEach { appExe ->
                        composeExeManifest
                            .manifestFile
                            .takeIf(File::exists)
                            ?.takeIf(File::isFile)
                            ?.inputStream()
                            ?.use {
                                appExe
                                    .resolveSibling("${appExe.name}.manifest")
                                    .outputStream()
                                    .use(it::copyTo)
                            }
                    }
            }
        }


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
            it.finalizedBy(embedManifestInExe)
        }


        // project.tasks.register("SampleTask", MyTask::class.java) {
        //     println("Hello world!")
        // }
    }
}

//abstract class MyTask : DefaultTask() {
//    @get:Input
//    abstract val fileText: Property<String>
//
//    @Input
//    val fileName = project.rootDir.toString() + "/myfile.txt"
//
//    @OutputFile
//    val myFile: File = File(fileName)
//
//    @TaskAction
//    fun action() {
//        myFile.createNewFile()
//        myFile.writeText(fileText.get())
//    }
//}
