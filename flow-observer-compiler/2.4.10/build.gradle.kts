import org.gradle.plugins.signing.SigningExtension

plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

val kotlinCompilerVersion = "2.4.10"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        // KGP in this build is 2.2.10; embeddable 2.4.x metadata needs this to compile the plugin.
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

sourceSets {
    main {
        kotlin.srcDir("../src/common/kotlin")
        resources.srcDir("../src/common/resources")
        kotlin.srcDir("../src/ir-modern/kotlin")
        kotlin.srcDir("../src/registrar23/kotlin")
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
