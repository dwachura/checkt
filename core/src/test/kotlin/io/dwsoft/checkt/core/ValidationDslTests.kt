package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.alwaysFailWithMessage
import io.dwsoft.checkt.testing.shouldContainMessages
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.take

class ValidationDslTests : StringSpec({
    val validated = Dto(
        text = Arb.string().next(),
        list = Arb.set(Arb.int(), 3).next().toList(),
        nested = Dto.Nested(
            Arb.string().next()
        )
    )

    "basic validation works correctly" {
        val validationException = shouldThrow<ValidationFailure> {
            validate(validated) {
                text must alwaysFailWithMessage { "[${validationPath()}] validate(root) - $value" }
                ::nested {
                    text must alwaysFailWithMessage { "[${validationPath()}] validate(property) - $value" }
                }
                nested(namedAs = "custom name") {
                    text must alwaysFailWithMessage { "[${validationPath()}] validate(value) - $value" }
                }
            }.throwIfFailure()
        }

        validationException.shouldContainMessages(
            "[] validate(root) - ${validated.text}",
            "[nested] validate(property) - ${validated.nested.text}",
            "[custom name] validate(value) - ${validated.nested.text}",
        )
    }

    "iterables validation works correctly" {
        val list = (1..3).toList()
        val expectedElementErrorMessages =
            list.mapIndexed { idx, v -> "list[$idx] - element $v validation" }

        val validationException = shouldThrow<ValidationFailure> {
            validate(list, namedAs = "list") {
                must(alwaysFailWithMessage { "${validationPath()} - list validation" })
                eachElement {
                    must(alwaysFailWithMessage { "${validationPath()} - element $value validation" })
                }
            }.throwIfFailure()
        }

        validationException.shouldContainMessages(
            "list - list validation",
            *expectedElementErrorMessages.toTypedArray()
        )
    }
})

private data class Dto(
    val text: String,
    val list: List<Int>,
    val nested: Nested,
) {
    data class Nested(val text: String)
}
