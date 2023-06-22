plugins {
    kotlin("jvm")
    `maven-publish` apply false
//    signing apply false
//    id("org.jmailen.kotlinter")
}

version = rootProject.version

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri(
            when {
                isSnapshot() -> "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                else -> "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            }
        )
    }
}

findProperty("checkt.testing.disabled") ?: run {
    dependencies {
        testImplementation(project(":testing"))
    }

    tasks.test {
        useJUnitPlatform()
        systemProperty("checkt.kotest.gradle-runtime", true)
    }
}

findProperty("checkt.maven.publishing.disabled") ?: run {
    apply { plugin(MavenPublishPlugin::class) }
    apply { plugin(SigningPlugin::class) }

    val artifactName = artifactName()

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "${rootProject.group}"
                artifactId = artifactName
                from(components["java"])
            }
        }

        repositories {
            maven {
                url = uri(
                    when {
                        isSnapshot() -> "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                        else -> "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                    }
                )
                credentials {
                    username = ""
                    password = ""
                }
            }
        }
    }

//    signing {
//        sign(publishing.publications["maven"])
//    }

    tasks.jar {
        archiveBaseName.set(artifactName)
    }
}
