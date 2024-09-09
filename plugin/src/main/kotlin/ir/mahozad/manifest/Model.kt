package ir.mahozad.manifest

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

    internal val shouldEmbed get() = this == EMBED || this == COPY_AND_EMBED
}
