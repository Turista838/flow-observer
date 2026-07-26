pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "flow-observer"
include(
    ":flow-observer",
    ":flow-observer-compiler-2.0.10",
    ":flow-observer-compiler-2.0.21",
    ":flow-observer-compiler-2.1.10",
    ":flow-observer-compiler-2.2.10",
    ":flow-observer-compiler-2.2.21",
    ":flow-observer-compiler-2.3.21",
    ":flow-observer-compiler-2.4.10",
    ":sample",
)

// Thin versioned modules; shared IR sources stay in flow-observer-compiler/src.
project(":flow-observer-compiler-2.0.10").projectDir = file("flow-observer-compiler/2.0.10")
project(":flow-observer-compiler-2.0.21").projectDir = file("flow-observer-compiler/2.0.21")
project(":flow-observer-compiler-2.1.10").projectDir = file("flow-observer-compiler/2.1.10")
project(":flow-observer-compiler-2.2.10").projectDir = file("flow-observer-compiler/2.2.10")
project(":flow-observer-compiler-2.2.21").projectDir = file("flow-observer-compiler/2.2.21")
project(":flow-observer-compiler-2.3.21").projectDir = file("flow-observer-compiler/2.3.21")
project(":flow-observer-compiler-2.4.10").projectDir = file("flow-observer-compiler/2.4.10")
