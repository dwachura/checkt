package io.dwsoft.checkt.testing

import io.dwsoft.checkt.core.ValidationPath
import io.dwsoft.checkt.core.Violation
import io.dwsoft.checkt.core.joinToString
import io.dwsoft.checkt.core.name
import io.kotest.matchers.shouldBe

interface ViolationAssertionsDsl {
    val expectedValidationPath: ValidationPath
    val errorMessageAssertions: (String) -> Unit

    fun underPath(f: ValidationPathBuilder)
    fun withMessageThat(f: (String) -> Unit)
    fun withMessage(expectedMessage: String)
}

fun ViolationAssertionsDsl() =
    object : ViolationAssertionsDsl {
        override var expectedValidationPath: ValidationPath = validationPath { root }
            private set
        override var errorMessageAssertions: (String) -> Unit = {}
            private set

        override fun underPath(f: ValidationPathBuilder) {
            expectedValidationPath = validationPath(f)
        }

        override fun withMessageThat(f: (String) -> Unit) {
            errorMessageAssertions = f
        }

        override fun withMessage(expectedMessage: String) {
            errorMessageAssertions = { it shouldBe expectedMessage }
        }
    }

fun Violation<*, *>.debugString() =
    buildString {
        append("{ ")
        append("rule: '${context.descriptor.name}', ")
        append("path: '${context.path.joinToString()}', ")
        append("value: '$value', ")
        append("message: '$errorMessage', ")
        append(" }")
    }