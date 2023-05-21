plugins {
    id("checkt-subproject-base")
}

dependencies {
    implementation(project(":core:checks"))
    implementation(Dependencies.`kotlinx-coroutines-core`)
}
