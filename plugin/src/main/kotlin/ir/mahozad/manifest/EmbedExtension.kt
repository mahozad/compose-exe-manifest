package ir.mahozad.manifest

import org.gradle.api.Project
import java.io.File
import javax.inject.Inject

abstract class EmbedExtension @Inject constructor(project: Project) {
    /**
     * Whether the embedding/copying is enabled.
     *
     * Defaults to `true`.
     */
    val enabled = project.objects.property(Boolean::class.java).value(true)

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
