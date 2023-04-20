plugins {
    id("checkt-subproject-base")
}

dependencies {
    api(project(":core"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-RC")
}
