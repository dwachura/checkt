import org.gradle.api.Project

fun Project.artifactName(): String =
    when {
        parent == rootProject || parent == null -> name
        else -> "${(parent as Project).name}-${name}"
    }
