import org.gradle.api.Project

fun Project.artifactName(): String =
    when {
        this == rootProject || parent == null -> name
        else -> "${parent!!.artifactName()}-$name"
    }
