import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.provideDelegate

object Dependencies {
    val `kotlin-logging` = "io.github.microutils:kotlin-logging:${Versions.`kotlin-logging`}"
    val `logback-classic` = "ch.qos.logback:logback-classic:${Versions.`logback-classic`}"
    val `kotest-runner-junit5` = "io.kotest:kotest-runner-junit5:${Versions.kotest}"
    val `kotest-assertions-core` = "io.kotest:kotest-assertions-core:${Versions.kotest}"
    val `kotest-property` = "io.kotest:kotest-property:${Versions.kotest}"
    val mockk = "io.mockk:mockk:${Versions.mockk}"

    object Versions {
        val `kotlin-logging` = "2.1.23"
        val `logback-classic` = "1.2.11"
        val `kotest` = "5.3.1"
        val mockk = "1.12.4"
    }
}
