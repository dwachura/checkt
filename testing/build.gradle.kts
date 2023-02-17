plugins {
    id("checkt-subproject-base")
}

dependencies {
    api(project(":core"))

    api(Dependencies.`kotest-runner-junit5`)
    api(Dependencies.`kotest-assertions-core`)
    api(Dependencies.`kotest-property`)
    api(Dependencies.mockk)
}
