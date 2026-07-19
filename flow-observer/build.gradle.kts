import org.gradle.plugins.signing.SigningExtension

plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}

extensions.configure<SigningExtension>("signing") {
    useGpgCmd()
}
