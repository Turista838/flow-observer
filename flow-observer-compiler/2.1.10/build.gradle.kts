import org.gradle.plugins.signing.SigningExtension

plugins {
    id("java-library")
    // One KGP version per Gradle build; host Kotlin for this jar is the embeddable below.
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

val kotlinCompilerVersion = "2.1.10"

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

// Shared roots under flow-observer-compiler/src; family selects IR + registrar variants.
sourceSets {
    main {
        kotlin.srcDir("../src/common/kotlin")
        resources.srcDir("../src/common/resources")
        kotlin.srcDir("../src/ir-legacy21/kotlin")
        kotlin.srcDir("../src/registrar/kotlin")
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
