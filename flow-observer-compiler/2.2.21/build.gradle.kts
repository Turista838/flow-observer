import org.gradle.plugins.signing.SigningExtension

plugins {
    id("java-library")
    // KGP must match the rest of this Gradle build (only one org.jetbrains.kotlin.jvm
    // version is allowed). The *host* Kotlin this jar targets is pinned via embeddable below.
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

val kotlinCompilerVersion = libs.versions.kotlinCompiler2221.get()

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    }
}

// Shared plugin sources live one level up; this module only pins the embeddable version.
sourceSets {
    main {
        kotlin.srcDir("../src/main/kotlin")
        resources.srcDir("../src/main/resources")
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinCompilerVersion")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}

extensions.configure<SigningExtension>("signing") {
    useGpgCmd()
}
