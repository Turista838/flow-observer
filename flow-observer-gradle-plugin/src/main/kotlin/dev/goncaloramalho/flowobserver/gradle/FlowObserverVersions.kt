package dev.goncaloramalho.flowobserver.gradle

internal object FlowObserverVersions {
    val libraryVersion: String by lazy {
        val stream = FlowObserverVersions::class.java
            .classLoader
            .getResourceAsStream("flow-observer-gradle.properties")
            ?: error("Missing flow-observer-gradle.properties in plugin jar")
        stream.bufferedReader().useLines { lines ->
            lines.firstOrNull { it.startsWith("flowObserverVersion=") }
                ?.substringAfter("=")
                ?.trim()
                ?.takeIf { it.isNotEmpty() && !it.contains("\${") }
                ?: error("flowObserverVersion not set in flow-observer-gradle.properties")
        }
    }
}
