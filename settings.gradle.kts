rootProject.name = "checkt"

gradle.rootProject {
    group = "io.dwsoft.$name"
    version = "0.1.0-SNAPSHOT"
}

include("core", "testing", "core:checks", "samples")
