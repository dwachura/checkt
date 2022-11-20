package io.dwsoft.checkt.core

import io.dwsoft.checkt.testing.AlwaysFailingCheck
import io.dwsoft.checkt.testing.alwaysFailWithMessage
import io.dwsoft.checkt.testing.shouldFailBecause
import io.dwsoft.checkt.testing.violated
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string

class ValidationDslTests : StringSpec({
    val nested = ValidatedObject(
        simpleValue = Arb.string().next(),
        collection = Arb.set(Arb.int(), 3).next(),
        map = emptyMap(),
        complexValue = null,
    )
    val root = ValidatedObject(
        simpleValue = Arb.string().next(),
        collection = Arb.list(Arb.double(), 3..3).next(),
        map = Arb.map(Arb.double(), Arb.string(), maxSize = 5).next(),
        complexValue = nested,
    )

    "Root object's properties can be validated" {
        val validation = validate(root) {
            simpleValue must alwaysFailWithMessage { "1" }
            collection must alwaysFailWithMessage { "2" }
            complexValue must alwaysFailWithMessage { "3" }
        }

        validation.shouldFailBecause(
            root.simpleValue.violated<AlwaysFailingCheck>(withMessage = "1"),
            root.collection.violated<AlwaysFailingCheck>(withMessage = "2"),
            root.complexValue.violated<AlwaysFailingCheck>(withMessage = "3"),
        )
    }

    "Nested object's properties can be validated" {
        val expectedPath = validationPath { -"" / "nested" }
        val validation = validate(root) {
            complexValue!!.invoke(namedAs = "nested") {
                simpleValue must alwaysFailWithMessage { "1" }
                collection must alwaysFailWithMessage { "2" }
            }
        }

        validation.shouldFailBecause(
            nested.simpleValue.violated<AlwaysFailingCheck>(
                withMessage = "1",
                underPath = expectedPath
            ),
            nested.collection.violated<AlwaysFailingCheck>(
                withMessage = "2",
                underPath = expectedPath
            ),
        )
    }

    "Elements of iterable properties can be validated" {
        val validation = validate(root) {
            ::collection {
                eachElement { idx ->
                    this must alwaysFailWithMessage { "$idx" }
                }
            }
        }
        val expectedViolations = root.collection.mapIndexed { idx, elem ->
            elem.violated<AlwaysFailingCheck>(
                underPath = validationPath { -"" / "collection"[idx.idx] },
                withMessage = "$idx"
            )
        }.toTypedArray()

        validation.shouldFailBecause(*expectedViolations)
    }

    "Entries of maps can be validated" {
        val validation = validate(root) {
            ::map {
                eachEntry(indexedUsingKeysTransformedBy = { "$it" }) {
                    key must alwaysFailWithMessage { "$key" }
                    value must alwaysFailWithMessage { "$value" }
                }
            }
        }
        val expectedViolations = root.map.map { (key, value) ->
            val expectedPath = validationPath { -"" / "map"["$key"] }
            key.violated<AlwaysFailingCheck>(withMessage = "$key", underPath = expectedPath)
            value.violated<AlwaysFailingCheck>(withMessage = value, underPath = expectedPath)
        }.toTypedArray()

        validation.shouldFailBecause(*expectedViolations)
    }
})

private data class ValidatedObject(
    val simpleValue: String,
    val collection: Collection<Number>,
    val map: Map<Double, String>,
    val complexValue: ValidatedObject?,
)
