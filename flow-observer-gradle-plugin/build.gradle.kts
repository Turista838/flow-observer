import org.gradle.plugins.signing.SigningExtension

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.2.10")
}

gradlePlugin {
    plugins {
        create("flowObserver") {
            id = "dev.goncaloramalho.flow-observer"
            displayName = "Flow Observer"
            description =
                "Adds flow-observer runtime and the Kotlin compiler plugin matching this project's Kotlin version"
            implementationClass =
                "dev.goncaloramalho.flowobserver.gradle.FlowObserverGradlePlugin"
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    val libraryVersion = version.toString()
    filesMatching("**/flow-observer-gradle.properties") {
        filter { line: String ->
            line.replace("\${flowObserverVersion}", libraryVersion)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}

extensions.configure<SigningExtension>("signing") {
    useGpgCmd()
}
