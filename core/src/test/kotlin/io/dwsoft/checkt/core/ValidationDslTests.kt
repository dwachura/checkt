package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.alwaysFailingRule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll

class ValidationDslTests : StringSpec({
    "dsl works correctly" {
        val validated = Dto("str", Dto.Nested("str-2"))

        val validationException = shouldThrow<ValidationFailure> {
            validate(validated) {
                text must alwaysFailingRule(errorMessage { "[${validationPath()}] validate(root) - $value" })
                ::nested {
                    text must alwaysFailingRule(errorMessage { "[${validationPath()}] validate(property) - $value" })
                }
                nested(namedAs = "custom name".toDisplayed()) {
                    text must alwaysFailingRule(errorMessage { "[${validationPath()}] validate(value) - $value" })
                }
            }.throwIfFailure()
        }

        validationException.errors
            .map { it.errorDetails.value }
            .shouldContainAll(
                listOf(
                    "[] validate(root) - str",
                    "[nested] validate(property) - str-2",
                    "[custom name] validate(value) - str-2",
                )
            )
    }
})

private data class Dto(
    val text: String,
    val nested: Nested,
) {
    data class Nested(val text: String)
}
