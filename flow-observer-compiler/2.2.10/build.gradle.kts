import org.gradle.plugins.signing.SigningExtension

plugins {
    id("java-library")
    // One KGP version per Gradle build; host Kotlin for this jar is the embeddable below.
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

// Same Kotlin as the consumer host this jar targets.
val kotlinCompilerVersion = libs.versions.kotlin.get()

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

// Shared plugin sources live one level up; this module only pins the Kotlin version.
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
