package dev.goncaloramalho.flowobserver.gradle

/**
 * Maps a consumer Kotlin version to the published `flow-observer-compiler-<suffix>` artifact id suffix.
 * Ranges come from host↔plugin compatibility probes.
 */
internal object CompilerArtifactMapping {

    private val supported: Map<String, String> = mapOf(
        "2.0.0" to "2.0.10",
        "2.0.10" to "2.0.10",
        "2.0.20" to "2.0.21",
        "2.0.21" to "2.0.21",
        "2.1.0" to "2.1.10",
        "2.1.10" to "2.1.10",
        "2.1.20" to "2.1.10",
        "2.1.21" to "2.1.10",
        "2.2.0" to "2.2.10",
        "2.2.10" to "2.2.10",
        "2.2.20" to "2.2.21",
        "2.2.21" to "2.2.21",
        "2.3.0" to "2.3.21",
        "2.3.10" to "2.3.21",
        "2.3.20" to "2.3.21",
        "2.3.21" to "2.3.21",
        "2.4.0" to "2.4.10",
        "2.4.10" to "2.4.10",
    )

    fun compilerArtifactSuffix(kotlinVersion: String): String {
        val normalized = kotlinVersion.substringBefore("-").trim()
        return supported[normalized]
            ?: error(
                "flow-observer does not support Kotlin $kotlinVersion. " +
                    "Supported versions: ${supported.keys.sorted().joinToString()}. " +
                    "Add a matching flow-observer-compiler artifact or upgrade flow-observer.",
            )
    }

    val supportedKotlinVersions: Set<String> get() = supported.keys
}
