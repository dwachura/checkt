import org.gradle.api.Project

fun Project.artifactName(): String =
    when {
        this == rootProject || parent == null -> name
        else -> "${parent!!.artifactName()}-$name"
    }

fun Project.isSnapshot(): Boolean = "$version".endsWith("SNAPSHOT", ignoreCase = true)
