import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish` apply false
//    id("org.jmailen.kotlinter")
}

version = rootProject.version

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(Dependencies.`kotlin-logging`)
    testRuntimeOnly(Dependencies.`logback-classic`)
}

findProperty("checkt.testing.disabled") ?: run {
    dependencies {
        testImplementation(project(":testing"))
    }

    tasks.test {
        useJUnitPlatform()
    }
}

findProperty("checkt.maven.publishing.disabled") ?: run {
    apply { plugin("org.gradle.maven-publish") }

    val artifactName = "${rootProject.name}-${project.name}"

    publishing {
        publications {
            create<MavenPublication>("maven") {
                artifactId = artifactName
                from(components["java"])
            }
        }
    }

    tasks.jar {
        archiveBaseName.set(artifactName)
    }
}
