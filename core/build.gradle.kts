plugins {
    id("checkt-subproject-base")
}

dependencies {
    implementation(Dependencies.`kotlinx-coroutines-core`)
    testImplementation(kotlin("reflect"))
}
