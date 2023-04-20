plugins {
    id("checkt-subproject-base")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines:0.19.2")
    testImplementation(kotlin("reflect"))
}
