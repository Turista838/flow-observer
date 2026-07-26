package dev.goncaloramalho.flowobserver.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

/**
 * Wires `flow-observer` runtime + the compiler plugin jar that matches this project's Kotlin version.
 *
 * ```
 * plugins {
 *   id("org.jetbrains.kotlin.android") version "2.2.21"
 *   id("dev.goncaloramalho.flow-observer") version "2.0.0"
 * }
 * ```
 */
class FlowObserverGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        var wired = false

        project.plugins.withType(KotlinBasePlugin::class.java).configureEach {
            if (!wired) {
                project.wireFlowObserver(pluginVersion)
                wired = true
            }
        }

        project.afterEvaluate {
            if (!wired) {
                error(
                    "flow-observer Gradle plugin requires a Kotlin Gradle plugin " +
                        "(org.jetbrains.kotlin.android, .jvm, or .multiplatform) on the same project. " +
                        "Apply the Kotlin plugin before or alongside id(\"dev.goncaloramalho.flow-observer\").",
                )
            }
        }
    }
}

private fun Project.wireFlowObserver(kotlinVersion: String) {
    val libraryVersion = FlowObserverVersions.libraryVersion
    val compilerSuffix = CompilerArtifactMapping.compilerArtifactSuffix(kotlinVersion)

    val runtimeProject = rootProject.findProject(":flow-observer")
    if (runtimeProject != null) {
        dependencies.add("implementation", runtimeProject)
    } else {
        dependencies.add(
            "implementation",
            "dev.goncaloramalho:flow-observer:$libraryVersion",
        )
    }

    val compilerProject = rootProject.findProject(":flow-observer-compiler-$compilerSuffix")
    if (compilerProject != null) {
        dependencies.add("kotlinCompilerPluginClasspath", compilerProject)
    } else {
        dependencies.add(
            "kotlinCompilerPluginClasspath",
            "dev.goncaloramalho:flow-observer-compiler-$compilerSuffix:$libraryVersion",
        )
    }

    logger.lifecycle(
        "flow-observer: Kotlin $kotlinVersion → " +
            "flow-observer-compiler-$compilerSuffix:$libraryVersion",
    )
}
