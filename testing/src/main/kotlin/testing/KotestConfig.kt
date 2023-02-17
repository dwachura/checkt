package io.dwsoft.checkt.testing

import io.kotest.core.config.AbstractProjectConfig

internal object KotestConfig : AbstractProjectConfig() {
    init {
        // Support for nested test names in test reports generated during gradle build
        displayFullTestPath = System.getProperty("checkt.kotest.gradle-runtime", "false").toBoolean()
    }
}
